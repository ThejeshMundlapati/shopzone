package com.shopzone.model;

import com.shopzone.model.enums.PaymentMethod;
import com.shopzone.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a payment transaction.
 * Stores Stripe payment details and tracks payment lifecycle.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_order_id", columnList = "order_id"),
    @Index(name = "idx_payment_intent_id", columnList = "stripe_payment_intent_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_user_id", columnList = "user_id")
})
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;


  @Column(name = "order_id", nullable = false)
  private String orderId;

  @Column(name = "order_number", nullable = false)
  private String orderNumber;

  @Column(name = "user_id", nullable = false)
  private String userId;


  /**
   * Stripe PaymentIntent ID (pi_xxx)
   */
  @Column(name = "stripe_payment_intent_id", unique = true)
  private String stripePaymentIntentId;

  /**
   * Stripe Charge ID (ch_xxx) - populated after successful payment
   */
  @Column(name = "stripe_charge_id")
  private String stripeChargeId;

  /**
   * Stripe Customer ID (cus_xxx) - if customer was created
   */
  @Column(name = "stripe_customer_id")
  private String stripeCustomerId;

  /**
   * Client secret for frontend payment confirmation
   */
  @Column(name = "client_secret", length = 500)
  private String clientSecret;


  @Column(precision = 10, scale = 2, nullable = false)
  private BigDecimal amount;

  @Column(length = 3, nullable = false)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method")
  private PaymentMethod paymentMethod;

  /**
   * Last 4 digits of card (if card payment)
   */
  @Column(name = "card_last_four", length = 4)
  private String cardLastFour;

  /**
   * Card brand (visa, mastercard, etc.)
   */
  @Column(name = "card_brand", length = 20)
  private String cardBrand;


  /**
   * Stripe error code if payment failed
   */
  @Column(name = "failure_code")
  private String failureCode;

  /**
   * Human-readable failure message
   */
  @Column(name = "failure_message", length = 500)
  private String failureMessage;


  /**
   * Total amount refunded
   */
  @Column(name = "amount_refunded", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal amountRefunded = BigDecimal.ZERO;

  /**
   * Stripe Refund ID (re_xxx) for the last refund
   */
  @Column(name = "stripe_refund_id")
  private String stripeRefundId;

  /**
   * Reason for refund
   */
  @Column(name = "refund_reason", length = 500)
  private String refundReason;


  /**
   * Receipt URL from Stripe
   */
  @Column(name = "receipt_url", length = 500)
  private String receiptUrl;

  /**
   * Description shown on customer's statement
   */
  @Column(name = "statement_descriptor", length = 22)
  private String statementDescriptor;


  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /**
   * When payment was successfully completed
   */
  @Column(name = "paid_at")
  private LocalDateTime paidAt;

  /**
   * When refund was processed
   */
  @Column(name = "refunded_at")
  private LocalDateTime refundedAt;


  /**
   * Check if payment can be refunded.
   */
  public boolean canRefund() {
    return status.canRefund() &&
        amount.subtract(amountRefunded).compareTo(BigDecimal.ZERO) > 0;
  }

  /**
   * Get remaining refundable amount.
   */
  public BigDecimal getRefundableAmount() {
    return amount.subtract(amountRefunded);
  }

  /**
   * Check if this is a full refund.
   */
  public boolean isFullyRefunded() {
    return amountRefunded.compareTo(amount) >= 0;
  }

  /**
   * Record a successful payment.
   */
  public void markAsPaid(String chargeId, String receiptUrl) {
    this.status = PaymentStatus.PAID;
    this.stripeChargeId = chargeId;
    this.receiptUrl = receiptUrl;
    this.paidAt = LocalDateTime.now();
  }

  /**
   * Record a failed payment.
   */
  public void markAsFailed(String failureCode, String failureMessage) {
    this.status = PaymentStatus.FAILED;
    this.failureCode = failureCode;
    this.failureMessage = failureMessage;
  }

  /**
   * Record a refund.
   */
  public void recordRefund(BigDecimal refundAmount, String refundId, String reason) {
    this.amountRefunded = this.amountRefunded.add(refundAmount);
    this.stripeRefundId = refundId;
    this.refundReason = reason;
    this.refundedAt = LocalDateTime.now();

    if (isFullyRefunded()) {
      this.status = PaymentStatus.REFUNDED;
    } else {
      this.status = PaymentStatus.PARTIALLY_REFUNDED;
    }
  }
}