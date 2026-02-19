package com.shopzone.service;

import com.shopzone.config.StripeConfig;
import com.shopzone.dto.response.PaymentIntentResponse;
import com.shopzone.dto.response.PaymentResponse;
import com.shopzone.controller.AdminPaymentController.PaymentStats;
import com.shopzone.exception.BadRequestException;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.Order;
import com.shopzone.model.Payment;
import com.shopzone.model.User;
import com.shopzone.model.enums.PaymentMethod;
import com.shopzone.model.enums.PaymentStatus;
import com.shopzone.repository.jpa.PaymentRepository;
import com.shopzone.repository.jpa.UserRepository;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for payment operations.
 * Coordinates between orders, Stripe, and payment records.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final UserRepository userRepository;
  private final StripeService stripeService;
  private final StripeConfig stripeConfig;
  private final EmailService emailService;

  private final @Lazy OrderService orderService;

  /**
   * Create a payment intent for an order.
   * Called during checkout after order is created.
   */
  @Transactional
  public PaymentIntentResponse createPaymentIntent(String orderNumber, String userId) {
    log.info("Creating payment intent for order: {}", orderNumber);

    Order order = orderService.getOrderByNumber(orderNumber);

    if (!order.getUserId().equals(userId)) {
      throw new BadRequestException("Order does not belong to this user");
    }

    if (order.getPaymentStatus() == PaymentStatus.PAID) {
      throw new BadRequestException("Order is already paid");
    }

    if (order.getStripePaymentIntentId() != null) {
      try {
        PaymentIntent existingIntent = stripeService.retrievePaymentIntent(order.getStripePaymentIntentId());
        if ("requires_payment_method".equals(existingIntent.getStatus()) ||
            "requires_confirmation".equals(existingIntent.getStatus())) {
          log.info("Returning existing PaymentIntent for order: {}", orderNumber);
          return PaymentIntentResponse.builder()
              .paymentIntentId(existingIntent.getId())
              .clientSecret(existingIntent.getClientSecret())
              .publishableKey(stripeConfig.getPublicKey())
              .orderNumber(orderNumber)
              .amount(order.getTotalAmount())
              .currency(stripeConfig.getCurrency())
              .status(PaymentStatus.AWAITING_PAYMENT.name())
              .build();
        }
      } catch (Exception e) {
        log.warn("Could not retrieve existing payment intent, creating new one: {}", e.getMessage());
      }
    }

    PaymentIntent intent = stripeService.createPaymentIntent(
        order.getTotalAmount(),
        order.getId(),
        order.getOrderNumber(),
        order.getUserEmail(),
        "Order " + order.getOrderNumber()
    );

    Payment payment = Payment.builder()
        .orderId(order.getId())
        .orderNumber(order.getOrderNumber())
        .userId(userId)
        .stripePaymentIntentId(intent.getId())
        .clientSecret(intent.getClientSecret())
        .amount(order.getTotalAmount())
        .currency(stripeConfig.getCurrency())
        .status(PaymentStatus.AWAITING_PAYMENT)
        .build();

    paymentRepository.save(payment);

    order.setStripePaymentIntentId(intent.getId());
    order.setPaymentStatus(PaymentStatus.AWAITING_PAYMENT);
    orderService.saveOrder(order);

    log.info("Created PaymentIntent {} for order {}", intent.getId(), orderNumber);

    return PaymentIntentResponse.builder()
        .paymentIntentId(intent.getId())
        .clientSecret(intent.getClientSecret())
        .publishableKey(stripeConfig.getPublicKey())
        .orderNumber(orderNumber)
        .amount(order.getTotalAmount())
        .currency(stripeConfig.getCurrency())
        .status(PaymentStatus.AWAITING_PAYMENT.name())
        .build();
  }

  /**
   * Get payment status for an order.
   */
  @Transactional(readOnly = true)
  public PaymentResponse getPaymentStatus(String orderNumber, String userId) {
    Order order = orderService.getOrderByNumber(orderNumber);

    if (userId != null && !order.getUserId().equals(userId)) {
      throw new BadRequestException("Order does not belong to this user");
    }

    Payment payment = paymentRepository.findByOrderNumber(orderNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderNumber));

    return PaymentResponse.fromEntity(payment);
  }

  /**
   * Get payment by Payment Intent ID.
   */
  @Transactional(readOnly = true)
  public Payment getPaymentByIntentId(String paymentIntentId) {
    return paymentRepository.findByStripePaymentIntentId(paymentIntentId)
        .orElseThrow(() -> new ResourceNotFoundException("Payment not found for intent: " + paymentIntentId));
  }

  /**
   * Handle successful payment (called by webhook).
   */
  @Transactional
  public void handlePaymentSuccess(String paymentIntentId, String chargeId, String receiptUrl,
                                   String cardLast4, String cardBrand) {
    log.info("Processing successful payment for intent: {}", paymentIntentId);

    Payment payment = getPaymentByIntentId(paymentIntentId);

    payment.setStatus(PaymentStatus.PAID);
    payment.setStripeChargeId(chargeId);
    payment.setReceiptUrl(receiptUrl);
    payment.setCardLastFour(cardLast4);
    payment.setCardBrand(cardBrand);
    payment.setPaymentMethod(PaymentMethod.CARD);
    payment.setPaidAt(LocalDateTime.now());

    paymentRepository.save(payment);

    Order order = orderService.getOrderById(payment.getOrderId());
    order.recordPayment(chargeId, receiptUrl);
    orderService.saveOrder(order);

    orderService.reduceStockForOrder(order);

    try {
      User user = userRepository.findById(UUID.fromString(payment.getUserId())).orElse(null);
      if (user != null) {
        log.info("Sending order confirmation email for order: {}", order.getOrderNumber());
        emailService.sendOrderConfirmation(order, user);
      } else {
        log.warn("User not found for order {}. Skipping email confirmation.", order.getOrderNumber());
      }
    } catch (Exception e) {
      log.error("Failed to send order confirmation email for order: {}", order.getOrderNumber(), e);
    }

    log.info("Payment completed for order: {}", payment.getOrderNumber());
  }

  /**
   * Handle failed payment (called by webhook).
   */
  @Transactional
  public void handlePaymentFailure(String paymentIntentId, String failureCode, String failureMessage) {
    log.info("Processing failed payment for intent: {}", paymentIntentId);

    Payment payment = getPaymentByIntentId(paymentIntentId);

    payment.setStatus(PaymentStatus.FAILED);
    payment.setFailureCode(failureCode);
    payment.setFailureMessage(failureMessage);

    paymentRepository.save(payment);

    Order order = orderService.getOrderById(payment.getOrderId());
    order.recordPaymentFailure();
    orderService.saveOrder(order);

    log.warn("Payment failed for order: {} - {}", payment.getOrderNumber(), failureMessage);
  }

  /**
   * Cancel payment for an order.
   */
  @Transactional
  public void cancelPayment(String orderNumber) {
    log.info("Cancelling payment for order: {}", orderNumber);

    Payment payment = paymentRepository.findByOrderNumber(orderNumber).orElse(null);

    if (payment != null && payment.getStripePaymentIntentId() != null &&
        !payment.getStatus().isTerminal()) {

      try {
        stripeService.cancelPaymentIntent(payment.getStripePaymentIntentId());
      } catch (Exception e) {
        log.warn("Could not cancel payment intent: {}", e.getMessage());
      }
      payment.setStatus(PaymentStatus.CANCELLED);
      paymentRepository.save(payment);
    }
  }

  /**
   * Get user's payment history.
   */
  @Transactional(readOnly = true)
  public Page<PaymentResponse> getUserPayments(String userId, Pageable pageable) {
    return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
        .map(PaymentResponse::fromEntity);
  }

  /**
   * Get all payments (admin).
   */
  @Transactional(readOnly = true)
  public Page<PaymentResponse> getAllPayments(PaymentStatus status, Pageable pageable) {
    if (status != null) {
      return paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
          .map(PaymentResponse::fromEntity);
    }
    return paymentRepository.findAll(pageable)
        .map(PaymentResponse::fromEntity);
  }

  /**
   * Check if a payment exists by PaymentIntent ID.
   * Used by webhook to verify if this is our payment.
   */
  public boolean existsByPaymentIntentId(String paymentIntentId) {
    return paymentRepository.existsByStripePaymentIntentId(paymentIntentId);
  }

  /**
   * Get payment statistics for admin dashboard.
   */
  @Transactional(readOnly = true)
  public PaymentStats getPaymentStatistics() {
    long totalPayments = paymentRepository.count();
    long successfulPayments = paymentRepository.countByStatus(PaymentStatus.PAID);
    long failedPayments = paymentRepository.countByStatus(PaymentStatus.FAILED);

    BigDecimal totalRevenue = BigDecimal.ZERO;
    BigDecimal totalRefunded = BigDecimal.ZERO;

    List<Payment> paidPayments = paymentRepository.findAll().stream()
        .filter(p -> p.getStatus() == PaymentStatus.PAID || p.getStatus() == PaymentStatus.REFUNDED || p.getStatus() == PaymentStatus.PARTIALLY_REFUNDED)
        .toList();

    for (Payment p : paidPayments) {
      if (p.getAmount() != null) {
        totalRevenue = totalRevenue.add(p.getAmount());
      }
      if (p.getAmountRefunded() != null) {
        totalRefunded = totalRefunded.add(p.getAmountRefunded());
      }
    }

    return PaymentStats.builder()
        .totalPayments(totalPayments)
        .successfulPayments(successfulPayments)
        .failedPayments(failedPayments)
        .totalRevenue(totalRevenue)
        .totalRefunded(totalRefunded)
        .build();
  }
}