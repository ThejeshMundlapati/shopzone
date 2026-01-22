package com.shopzone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for placing an order through checkout.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

  /**
   * ID of the shipping address to use (String UUID).
   */
  @NotBlank(message = "Shipping address is required")
  private String shippingAddressId;

  /**
   * Optional customer notes or special instructions.
   */
  @Size(max = 500, message = "Customer notes cannot exceed 500 characters")
  private String customerNotes;

  /**
   * Optional coupon code to apply (Future).
   */
  @Size(max = 50, message = "Coupon code cannot exceed 50 characters")
  private String couponCode;
}