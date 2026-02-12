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
 * Summary view of a payment for list displays.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment summary for list views")
public class PaymentSummaryResponse {

  @Schema(description = "Payment ID")
  private String id;

  @Schema(description = "Order number", example = "ORD-20260129-0001")
  private String orderNumber;

  @Schema(description = "Amount", example = "99.99")
  private BigDecimal amount;

  @Schema(description = "Currency", example = "usd")
  private String currency;

  @Schema(description = "Payment status", example = "PAID")
  private String status;

  @Schema(description = "Payment method", example = "CARD")
  private String paymentMethod;

  @Schema(description = "Card last 4 (if card)", example = "4242")
  private String cardLastFour;

  @Schema(description = "When payment was created")
  private LocalDateTime createdAt;

  @Schema(description = "When payment was completed")
  private LocalDateTime paidAt;

  /**
   * Create summary from Payment entity.
   */
  public static PaymentSummaryResponse fromEntity(Payment payment) {
    return PaymentSummaryResponse.builder()
        .id(payment.getId())
        .orderNumber(payment.getOrderNumber())
        .amount(payment.getAmount())
        .currency(payment.getCurrency())
        .status(payment.getStatus().name())
        .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)
        .cardLastFour(payment.getCardLastFour())
        .createdAt(payment.getCreatedAt())
        .paidAt(payment.getPaidAt())
        .build();
  }
}