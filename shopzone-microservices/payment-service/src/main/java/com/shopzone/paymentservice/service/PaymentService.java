package com.shopzone.paymentservice.service;

import com.shopzone.common.exception.*;
import com.shopzone.paymentservice.client.OrderClient;
import com.shopzone.paymentservice.config.StripeConfig;
import com.shopzone.paymentservice.model.Payment;
import com.shopzone.paymentservice.model.enums.*;
import com.shopzone.paymentservice.repository.PaymentRepository;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service @RequiredArgsConstructor @Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final StripeConfig stripeConfig;
    private final OrderClient orderClient;

    @Transactional
    public Map<String, Object> createPaymentIntent(String orderId, String orderNumber,
            String userId, String userEmail, BigDecimal amount) {
        PaymentIntent intent = stripeService.createPaymentIntent(amount, orderId, orderNumber, userEmail, "Order " + orderNumber);

        Payment payment = Payment.builder()
            .orderId(orderId).orderNumber(orderNumber).userId(userId)
            .stripePaymentIntentId(intent.getId()).clientSecret(intent.getClientSecret())
            .amount(amount).currency(stripeConfig.getCurrency()).status(PaymentStatus.AWAITING_PAYMENT)
            .build();
        paymentRepository.save(payment);

        return Map.of("paymentIntentId", intent.getId(), "clientSecret", intent.getClientSecret(),
            "publishableKey", stripeConfig.getPublicKey(), "orderNumber", orderNumber,
            "amount", amount, "currency", stripeConfig.getCurrency(), "status", "AWAITING_PAYMENT");
    }

    @Transactional
    public void handlePaymentSuccess(String intentId, String chargeId, String receiptUrl, String cardLast4, String cardBrand) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(intentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found for intent: " + intentId));

        payment.markAsPaid(chargeId, receiptUrl);
        payment.setCardLastFour(cardLast4);
        payment.setCardBrand(cardBrand);
        payment.setPaymentMethod(PaymentMethod.CARD);
        paymentRepository.save(payment);

        // Notify Order Service
        orderClient.recordPayment(payment.getOrderId(), chargeId, receiptUrl);
        log.info("Payment success for order: {}", payment.getOrderNumber());
    }

    @Transactional
    public void handlePaymentFailure(String intentId, String code, String message) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(intentId).orElse(null);
        if (payment == null) return;
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureCode(code);
        payment.setFailureMessage(message);
        paymentRepository.save(payment);
        orderClient.recordPaymentFailure(payment.getOrderId());
    }

    public boolean existsByPaymentIntentId(String intentId) {
        return paymentRepository.existsByStripePaymentIntentId(intentId);
    }

    public Page<Map<String, Object>> getUserPayments(String userId, Pageable pageable) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(this::toPaymentMap);
    }

    public Page<Map<String, Object>> getAllPayments(PaymentStatus status, Pageable pageable) {
        Page<Payment> page = status != null
            ? paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
            : paymentRepository.findAll(pageable);
        return page.map(this::toPaymentMap);
    }

    public Map<String, Object> getPaymentStatus(String orderNumber) {
        Payment p = paymentRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return toPaymentMap(p);
    }

    private Map<String, Object> toPaymentMap(Payment p) {
        return Map.ofEntries(
            Map.entry("id", p.getId()), Map.entry("orderId", p.getOrderId()),
            Map.entry("orderNumber", p.getOrderNumber()), Map.entry("amount", p.getAmount()),
            Map.entry("currency", p.getCurrency()), Map.entry("status", p.getStatus().name()),
            Map.entry("cardLastFour", p.getCardLastFour() != null ? p.getCardLastFour() : ""),
            Map.entry("cardBrand", p.getCardBrand() != null ? p.getCardBrand() : ""),
            Map.entry("receiptUrl", p.getReceiptUrl() != null ? p.getReceiptUrl() : ""),
            Map.entry("createdAt", p.getCreatedAt().toString()),
            Map.entry("paidAt", p.getPaidAt() != null ? p.getPaidAt().toString() : "")
        );
    }
}
