package com.shopzone.dto.response;

import com.shopzone.model.Order;
import com.shopzone.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response containing payment intent details for frontend.
 * The clientSecret is used by Stripe.js to confirm payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment intent details for frontend payment processing")
public class PaymentIntentResponse {

  @Schema(description = "Stripe Payment Intent ID", example = "pi_3MtwBwLkdIwHu7ix28a3tqPa")
  private String paymentIntentId;

  @Schema(description = "Client secret for Stripe.js confirmation",
      example = "pi_3MtwBwLkdIwHu7ix28a3tqPa_secret_YrKJUKribcBjcG8HVhfZluoGH")
  private String clientSecret;

  @Schema(description = "Stripe publishable key for frontend", example = "pk_test_...")
  private String publishableKey;

  @Schema(description = "Order number", example = "ORD-20260129-0001")
  private String orderNumber;

  @Schema(description = "Amount to charge in dollars", example = "99.99")
  private BigDecimal amount;

  @Schema(description = "Currency code", example = "usd")
  private String currency;

  @Schema(description = "Current payment status", example = "AWAITING_PAYMENT")
  private String status;

  /**
   * Create response from Payment entity.
   */
  public static PaymentIntentResponse fromPayment(Payment payment, String publishableKey) {
    return PaymentIntentResponse.builder()
        .paymentIntentId(payment.getStripePaymentIntentId())
        .clientSecret(payment.getClientSecret())
        .publishableKey(publishableKey)
        .orderNumber(payment.getOrderNumber())
        .amount(payment.getAmount())
        .currency(payment.getCurrency())
        .status(payment.getStatus().name())
        .build();
  }

  /**
   * Create response from Order entity (during checkout).
   */
  public static PaymentIntentResponse fromOrder(Order order, String clientSecret, String publishableKey) {
    return PaymentIntentResponse.builder()
        .paymentIntentId(order.getStripePaymentIntentId())
        .clientSecret(clientSecret)
        .publishableKey(publishableKey)
        .orderNumber(order.getOrderNumber())
        .amount(order.getTotalAmount())
        .currency("usd")
        .status(order.getPaymentStatus().name())
        .build();
  }
}