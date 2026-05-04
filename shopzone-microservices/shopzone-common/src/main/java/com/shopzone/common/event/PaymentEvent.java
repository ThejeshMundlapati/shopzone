package com.shopzone.common.event;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Published by Payment Service to shopzone.payment.events topic.
 *
 * Event types:
 *   PAYMENT_CREATED — payment intent created, awaiting customer action
 *   PAYMENT_SUCCESS — payment completed via Stripe webhook
 *   PAYMENT_FAILED  — payment failed via Stripe webhook
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventType;
    private String eventId;
    private LocalDateTime timestamp;

    private String orderNumber;
    private String orderId;
    private String userId;

    // Payment details
    private String paymentId;
    private String stripePaymentIntentId;
    private String stripeChargeId;
    private BigDecimal amount;
    private String currency;
    private String receiptUrl;

    // Card details (set on success)
    private String cardLastFour;
    private String cardBrand;

    // Failure details (set on failure)
    private String failureCode;
    private String failureMessage;
}
