package com.shopzone.dto.request;

import com.shopzone.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

  @NotNull(message = "Status is required")
  private OrderStatus status;

  /**
   * Tracking number - required when status is SHIPPED
   */
  private String trackingNumber;

  /**
   * Shipping carrier (e.g., "FedEx", "UPS", "USPS")
   */
  private String shippingCarrier;

  /**
   * Admin notes for this status update
   */
  private String adminNotes;
}