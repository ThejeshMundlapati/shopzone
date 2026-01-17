package com.shopzone.dto.response;

import com.shopzone.model.CartItem;
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
public class CartItemResponse {

  private String productId;
  private String productName;
  private String productSlug;
  private BigDecimal price;
  private BigDecimal discountPrice;
  private BigDecimal effectivePrice;
  private int quantity;
  private String imageUrl;
  private int availableStock;
  private BigDecimal subtotal;
  private BigDecimal savings;
  private boolean isValid;
  private LocalDateTime addedAt;

  public static CartItemResponse fromCartItem(CartItem item) {
    return CartItemResponse.builder()
        .productId(item.getProductId())
        .productName(item.getProductName())
        .productSlug(item.getProductSlug())
        .price(item.getPrice())
        .discountPrice(item.getDiscountPrice())
        .effectivePrice(item.getEffectivePrice())
        .quantity(item.getQuantity())
        .imageUrl(item.getImageUrl())
        .availableStock(item.getAvailableStock())
        .subtotal(item.getSubtotal())
        .savings(item.getSavings())
        .isValid(item.isQuantityValid())
        .addedAt(item.getAddedAt())
        .build();
  }
}