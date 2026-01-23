package com.shopzone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for checkout preview/calculation.
 * Shows the user a breakdown of costs before placing the order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutPreviewResponse {

  /**
   * The cart being checked out.
   */
  private CartResponse cart;

  /**
   * Selected shipping address.
   */
  private AddressResponse shippingAddress;


  /**
   * Sum of all item prices.
   */
  private BigDecimal subtotal;

  /**
   * Total savings from item discounts.
   */
  private BigDecimal itemSavings;

  /**
   * Tax rate applied (as percentage, e.g., 8.0 for 8%).
   */
  private BigDecimal taxRate;

  /**
   * Calculated tax amount.
   */
  private BigDecimal taxAmount;

  /**
   * Shipping cost.
   */
  private BigDecimal shippingCost;

  /**
   * Whether free shipping was applied.
   */
  private boolean freeShipping;

  /**
   * Minimum order amount for free shipping.
   */
  private BigDecimal freeShippingThreshold;

  /**
   * Amount needed to reach free shipping (null if already qualified).
   */
  private BigDecimal amountToFreeShipping;

  /**
   * Discount from coupon code (future feature).
   */
  @Builder.Default
  private BigDecimal couponDiscount = BigDecimal.ZERO;

  /**
   * Applied coupon code (future feature).
   */
  private String appliedCoupon;

  /**
   * Final total amount.
   */
  private BigDecimal totalAmount;


  /**
   * Total number of items.
   */
  private int totalItems;

  /**
   * Number of unique products.
   */
  private int uniqueProducts;

  /**
   * Estimated delivery date (future feature).
   */
  private String estimatedDelivery;


  /**
   * Get total savings (item discounts + coupon).
   */
  public BigDecimal getTotalSavings() {
    BigDecimal savings = itemSavings != null ? itemSavings : BigDecimal.ZERO;
    BigDecimal coupon = couponDiscount != null ? couponDiscount : BigDecimal.ZERO;
    if (freeShipping && shippingCost != null && shippingCost.compareTo(BigDecimal.ZERO) > 0) {
      // If we're showing free shipping but there was a shipping cost, add it to savings
      // Actually, if freeShipping is true, shippingCost should already be 0
    }
    return savings.add(coupon);
  }

  /**
   * Check if any savings were applied.
   */
  public boolean hasSavings() {
    return getTotalSavings().compareTo(BigDecimal.ZERO) > 0;
  }
}