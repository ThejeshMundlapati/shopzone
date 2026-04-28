package com.shopzone.paymentservice.model;

import com.shopzone.paymentservice.model.enums.PaymentMethod;
import com.shopzone.paymentservice.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "payments", indexes = {
    @Index(name = "idx_payment_order_id", columnList = "order_id"),
    @Index(name = "idx_payment_intent", columnList = "stripe_payment_intent_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_user", columnList = "user_id")
})
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(name = "order_id", nullable = false) private String orderId;
    @Column(name = "order_number", nullable = false) private String orderNumber;
    @Column(name = "user_id", nullable = false) private String userId;
    @Column(name = "stripe_payment_intent_id", unique = true) private String stripePaymentIntentId;
    @Column(name = "stripe_charge_id") private String stripeChargeId;
    @Column(name = "client_secret", length = 500) private String clientSecret;
    @Column(precision = 10, scale = 2, nullable = false) private BigDecimal amount;
    @Column(length = 3, nullable = false) private String currency;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PaymentStatus status;
    @Enumerated(EnumType.STRING) @Column(name = "payment_method") private PaymentMethod paymentMethod;
    @Column(name = "card_last_four", length = 4) private String cardLastFour;
    @Column(name = "card_brand", length = 20) private String cardBrand;
    @Column(name = "failure_code") private String failureCode;
    @Column(name = "failure_message", length = 500) private String failureMessage;
    @Column(name = "amount_refunded", precision = 10, scale = 2) @Builder.Default private BigDecimal amountRefunded = BigDecimal.ZERO;
    @Column(name = "stripe_refund_id") private String stripeRefundId;
    @Column(name = "refund_reason", length = 500) private String refundReason;
    @Column(name = "receipt_url", length = 500) private String receiptUrl;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "paid_at") private LocalDateTime paidAt;
    @Column(name = "refunded_at") private LocalDateTime refundedAt;

    public boolean canRefund() { return status.canRefund() && amount.subtract(amountRefunded).compareTo(BigDecimal.ZERO) > 0; }
    public BigDecimal getRefundableAmount() { return amount.subtract(amountRefunded); }
    public boolean isFullyRefunded() { return amountRefunded.compareTo(amount) >= 0; }

    public void markAsPaid(String chargeId, String receiptUrl) {
        this.status = PaymentStatus.PAID; this.stripeChargeId = chargeId;
        this.receiptUrl = receiptUrl; this.paidAt = LocalDateTime.now();
    }

    public void recordRefund(BigDecimal refundAmount, String refundId, String reason) {
        this.amountRefunded = this.amountRefunded.add(refundAmount);
        this.stripeRefundId = refundId; this.refundReason = reason; this.refundedAt = LocalDateTime.now();
        this.status = isFullyRefunded() ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED;
    }
}
