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
public class WishlistItem implements Serializable {

  private static final long serialVersionUID = 1L;

  private String productId;
  private String productName;
  private String productSlug;
  private BigDecimal price;
  private BigDecimal discountPrice;
  private String imageUrl;
  private boolean inStock;
  private LocalDateTime addedAt;

  /**
   * Get effective price
   */
  public BigDecimal getEffectivePrice() {
    return discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0
        ? discountPrice
        : price;
  }

  /**
   * Check if product has discount
   */
  public boolean hasDiscount() {
    return discountPrice != null &&
        discountPrice.compareTo(BigDecimal.ZERO) > 0 &&
        discountPrice.compareTo(price) < 0;
  }

  /**
   * Calculate discount percentage
   */
  public int getDiscountPercentage() {
    if (!hasDiscount()) return 0;
    return price.subtract(discountPrice)
        .multiply(BigDecimal.valueOf(100))
        .divide(price, 0, java.math.RoundingMode.HALF_UP)
        .intValue();
  }
}