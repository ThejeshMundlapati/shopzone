package com.shopzone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for cancelling an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {

  /**
   * Reason for cancellation.
   * Required for record-keeping and analytics.
   */
  @NotBlank(message = "Cancellation reason is required")
  @Size(min = 10, max = 500, message = "Cancellation reason must be between 10 and 500 characters")
  private String reason;
}