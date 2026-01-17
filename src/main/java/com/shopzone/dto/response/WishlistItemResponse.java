package com.shopzone.dto.response;

import com.shopzone.model.WishlistItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {

  private String productId;
  private String productName;
  private String productSlug;
  private BigDecimal price;
  private BigDecimal discountPrice;
  private BigDecimal effectivePrice;
  private String imageUrl;
  private boolean inStock;
  private boolean hasDiscount;
  private int discountPercentage;
  private LocalDateTime addedAt;

  public static WishlistItemResponse fromWishlistItem(WishlistItem item) {
    return WishlistItemResponse.builder()
        .productId(item.getProductId())
        .productName(item.getProductName())
        .productSlug(item.getProductSlug())
        .price(item.getPrice())
        .discountPrice(item.getDiscountPrice())
        .effectivePrice(item.getEffectivePrice())
        .imageUrl(item.getImageUrl())
        .inStock(item.isInStock())
        .hasDiscount(item.hasDiscount())
        .discountPercentage(item.getDiscountPercentage())
        .addedAt(item.getAddedAt())
        .build();
  }
}