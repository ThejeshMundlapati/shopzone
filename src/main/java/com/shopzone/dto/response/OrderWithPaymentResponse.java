package com.shopzone.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Combined response for order with payment intent.
 * Used when placing an order with Stripe payment integration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order with payment intent details for checkout")
public class OrderWithPaymentResponse {

  @Schema(description = "Order details")
  private OrderResponse order;

  @Schema(description = "Payment intent details for frontend payment processing")
  private PaymentIntentResponse payment;
}