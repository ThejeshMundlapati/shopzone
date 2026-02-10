package com.shopzone.service;

import com.shopzone.dto.request.RefundRequest;
import com.shopzone.dto.response.RefundResponse;
import com.shopzone.exception.BadRequestException;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.Order;
import com.shopzone.model.Payment;
import com.shopzone.model.enums.OrderStatus;
import com.shopzone.model.enums.PaymentStatus;
import com.shopzone.repository.jpa.PaymentRepository;
import com.stripe.model.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service for processing refunds.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

  private final PaymentRepository paymentRepository;
  private final StripeService stripeService;
  private final OrderService orderService;
  private final ProductService productService;

  @Value("${payment.max-refund-days:30}")
  private int maxRefundDays;

  /**
   * Process a refund for an order.
   */
  @Transactional
  public RefundResponse processRefund(RefundRequest request) {
    log.info("Processing refund for order: {}", request.getOrderNumber());

    Order order = orderService.getOrderByNumber(request.getOrderNumber());
    Payment payment = paymentRepository.findByOrderNumber(request.getOrderNumber())
        .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + request.getOrderNumber()));

    validateRefund(order, payment, request);

    BigDecimal refundAmount = request.isFullRefund()
        ? payment.getRefundableAmount()
        : request.getAmount();

    if (refundAmount.compareTo(payment.getRefundableAmount()) > 0) {
      throw new BadRequestException("Refund amount exceeds refundable amount: " + payment.getRefundableAmount());
    }

    Refund stripeRefund;
    if (request.isFullRefund()) {
      stripeRefund = stripeService.createFullRefund(
          payment.getStripePaymentIntentId(),
          request.getReason()
      );
    } else {
      stripeRefund = stripeService.createPartialRefund(
          payment.getStripePaymentIntentId(),
          refundAmount,
          request.getReason()
      );
    }

    payment.recordRefund(refundAmount, stripeRefund.getId(), request.getReason());
    paymentRepository.save(payment);

    order.recordRefund(refundAmount);

    if (payment.isFullyRefunded()) {
      order.setStatus(OrderStatus.CANCELLED);
      order.setCancellationReason("Refund processed: " + request.getReason());
    }

    orderService.saveOrder(order);

    boolean stockRestored = false;
    if (request.isRestoreStock() && request.isFullRefund()) {
      orderService.restoreStockForOrder(order);
      stockRestored = true;
    }

    log.info("Refund processed for order: {} - Amount: {}", request.getOrderNumber(), refundAmount);

    return RefundResponse.builder()
        .refundId(stripeRefund.getId())
        .orderNumber(order.getOrderNumber())
        .amountRefunded(refundAmount)
        .totalRefunded(payment.getAmountRefunded())
        .remainingRefundable(payment.getRefundableAmount())
        .status(stripeRefund.getStatus())
        .currency(payment.getCurrency())
        .reason(request.getReason())
        .stockRestored(stockRestored)
        .orderStatus(order.getStatus().name())
        .paymentStatus(payment.getStatus().name())
        .refundedAt(LocalDateTime.now())
        .build();
  }

  /**
   * Validate that a refund can be processed.
   */
  private void validateRefund(Order order, Payment payment, RefundRequest request) {
    if (!payment.canRefund()) {
      throw new BadRequestException("Payment cannot be refunded. Status: " + payment.getStatus());
    }

    if (payment.getPaidAt() != null) {
      long daysSincePaid = ChronoUnit.DAYS.between(payment.getPaidAt(), LocalDateTime.now());
      if (daysSincePaid > maxRefundDays) {
        throw new BadRequestException("Refund window has expired. Maximum refund period is " + maxRefundDays + " days.");
      }
    }

    if (order.getStatus() == OrderStatus.REFUNDED) {
      throw new BadRequestException("Order has already been fully refunded");
    }

    if (!request.isFullRefund() && request.getAmount() != null) {
      if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BadRequestException("Refund amount must be positive");
      }
      if (request.getAmount().compareTo(payment.getRefundableAmount()) > 0) {
        throw new BadRequestException("Refund amount exceeds available balance: " + payment.getRefundableAmount());
      }
    }
  }

  /**
   * Get refund eligibility for an order.
   */
  @Transactional(readOnly = true)
  public RefundEligibility checkRefundEligibility(String orderNumber) {
    Order order = orderService.getOrderByNumber(orderNumber);
    Payment payment = paymentRepository.findByOrderNumber(orderNumber).orElse(null);

    if (payment == null) {
      return new RefundEligibility(false, BigDecimal.ZERO, "No payment found for order");
    }

    if (!payment.canRefund()) {
      return new RefundEligibility(false, BigDecimal.ZERO, "Payment status does not allow refunds: " + payment.getStatus());
    }

    if (payment.getPaidAt() != null) {
      long daysSincePaid = ChronoUnit.DAYS.between(payment.getPaidAt(), LocalDateTime.now());
      if (daysSincePaid > maxRefundDays) {
        return new RefundEligibility(false, BigDecimal.ZERO, "Refund window has expired");
      }
      int daysRemaining = maxRefundDays - (int) daysSincePaid;
      return new RefundEligibility(true, payment.getRefundableAmount(),
          "Refund available. " + daysRemaining + " days remaining in refund window.");
    }

    return new RefundEligibility(true, payment.getRefundableAmount(), "Refund available");
  }

  /**
   * Record for refund eligibility check.
   */
  public record RefundEligibility(boolean eligible, BigDecimal refundableAmount, String message) {}
}