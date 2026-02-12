package com.shopzone.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create a payment for an existing order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a payment intent for an order")
public class CreatePaymentRequest {

  @NotBlank(message = "Order number is required")
  @Schema(description = "Order number to create payment for", example = "ORD-20260129-0001")
  private String orderNumber;

  @Schema(description = "Save payment method for future use", example = "false")
  @Builder.Default
  private boolean savePaymentMethod = false;

  @Schema(description = "Customer email for receipt", example = "customer@example.com")
  private String receiptEmail;
}