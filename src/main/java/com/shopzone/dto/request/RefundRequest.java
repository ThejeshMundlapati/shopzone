package com.shopzone.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request to process a refund for an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to process a refund")
public class RefundRequest {

  @NotBlank(message = "Order number is required")
  @Schema(description = "Order number to refund", example = "ORD-20260129-0001")
  private String orderNumber;

  @Schema(description = "Amount to refund (null for full refund)", example = "25.00")
  @DecimalMin(value = "0.01", message = "Refund amount must be at least $0.01")
  private BigDecimal amount;

  @NotBlank(message = "Refund reason is required")
  @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
  @Schema(description = "Reason for the refund", example = "Customer requested cancellation")
  private String reason;

  @Schema(description = "Whether to restore stock for refunded items", example = "true")
  @Builder.Default
  private boolean restoreStock = true;

  /**
   * Check if this is a full refund request.
   */
  public boolean isFullRefund() {
    return amount == null;
  }
}