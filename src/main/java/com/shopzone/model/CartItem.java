package com.shopzone.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {

  private static final long serialVersionUID = 1L;

  private String productId;
  private String productName;
  private String productSlug;
  private BigDecimal price;
  private BigDecimal discountPrice;
  private int quantity;
  private String imageUrl;
  private int availableStock;
  private LocalDateTime addedAt;

  /**
   * Get effective price (discount price if available, otherwise regular price)
   */
  public BigDecimal getEffectivePrice() {
    return discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0
        ? discountPrice
        : price;
  }

  /**
   * Calculate subtotal for this item
   */
  public BigDecimal getSubtotal() {
    return getEffectivePrice().multiply(BigDecimal.valueOf(quantity));
  }

  /**
   * Check if quantity is within available stock
   */
  public boolean isQuantityValid() {
    return quantity > 0 && quantity <= availableStock;
  }

  /**
   * Calculate savings if discount is applied
   */
  public BigDecimal getSavings() {
    if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0) {
      return price.subtract(discountPrice).multiply(BigDecimal.valueOf(quantity));
    }
    return BigDecimal.ZERO;
  }
}