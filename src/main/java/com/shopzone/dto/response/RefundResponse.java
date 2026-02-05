package com.shopzone.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response for refund operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refund operation result")
public class RefundResponse {

  @Schema(description = "Stripe Refund ID", example = "re_3MtwBwLkdIwHu7ix0qHw")
  private String refundId;

  @Schema(description = "Order number", example = "ORD-20260129-0001")
  private String orderNumber;

  @Schema(description = "Amount refunded", example = "25.00")
  private BigDecimal amountRefunded;

  @Schema(description = "Total amount refunded for this order", example = "50.00")
  private BigDecimal totalRefunded;

  @Schema(description = "Remaining refundable amount", example = "49.99")
  private BigDecimal remainingRefundable;

  @Schema(description = "Refund status", example = "succeeded")
  private String status;

  @Schema(description = "Currency", example = "usd")
  private String currency;

  @Schema(description = "Reason for refund", example = "Customer requested cancellation")
  private String reason;

  @Schema(description = "Whether stock was restored", example = "true")
  private boolean stockRestored;

  @Schema(description = "New order status after refund", example = "CANCELLED")
  private String orderStatus;

  @Schema(description = "New payment status after refund", example = "REFUNDED")
  private String paymentStatus;

  @Schema(description = "When refund was processed")
  private LocalDateTime refundedAt;
}