package com.shopzone.dto.response;

import com.shopzone.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detailed payment information response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment details")
public class PaymentResponse {

  @Schema(description = "Payment ID", example = "pay_abc123")
  private String id;

  @Schema(description = "Order ID", example = "ord_xyz789")
  private String orderId;

  @Schema(description = "Order number", example = "ORD-20260129-0001")
  private String orderNumber;

  @Schema(description = "Stripe Payment Intent ID", example = "pi_3MtwBwLkdIwHu7ix28a3tqPa")
  private String stripePaymentIntentId;

  @Schema(description = "Stripe Charge ID", example = "ch_3MtwBwLkdIwHu7ix0qHw")
  private String stripeChargeId;

  @Schema(description = "Payment amount", example = "99.99")
  private BigDecimal amount;

  @Schema(description = "Currency", example = "usd")
  private String currency;

  @Schema(description = "Payment status", example = "PAID")
  private String status;

  @Schema(description = "Payment method type", example = "CARD")
  private String paymentMethod;

  @Schema(description = "Card last 4 digits", example = "4242")
  private String cardLastFour;

  @Schema(description = "Card brand", example = "visa")
  private String cardBrand;

  @Schema(description = "Amount refunded", example = "0.00")
  private BigDecimal amountRefunded;

  @Schema(description = "Refundable amount remaining", example = "99.99")
  private BigDecimal refundableAmount;

  @Schema(description = "Receipt URL", example = "https://pay.stripe.com/receipts/...")
  private String receiptUrl;

  @Schema(description = "Failure code if payment failed", example = "card_declined")
  private String failureCode;

  @Schema(description = "Failure message", example = "Your card was declined")
  private String failureMessage;

  @Schema(description = "When payment was created")
  private LocalDateTime createdAt;

  @Schema(description = "When payment was completed")
  private LocalDateTime paidAt;

  @Schema(description = "When refund was processed")
  private LocalDateTime refundedAt;

  /**
   * Create response from Payment entity.
   */
  public static PaymentResponse fromEntity(Payment payment) {
    return PaymentResponse.builder()
        .id(payment.getId())
        .orderId(payment.getOrderId())
        .orderNumber(payment.getOrderNumber())
        .stripePaymentIntentId(payment.getStripePaymentIntentId())
        .stripeChargeId(payment.getStripeChargeId())
        .amount(payment.getAmount())
        .currency(payment.getCurrency())
        .status(payment.getStatus().name())
        .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)
        .cardLastFour(payment.getCardLastFour())
        .cardBrand(payment.getCardBrand())
        .amountRefunded(payment.getAmountRefunded())
        .refundableAmount(payment.getRefundableAmount())
        .receiptUrl(payment.getReceiptUrl())
        .failureCode(payment.getFailureCode())
        .failureMessage(payment.getFailureMessage())
        .createdAt(payment.getCreatedAt())
        .paidAt(payment.getPaidAt())
        .refundedAt(payment.getRefundedAt())
        .build();
  }
}