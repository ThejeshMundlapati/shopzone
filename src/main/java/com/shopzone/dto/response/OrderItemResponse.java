package com.shopzone.dto.response;

import com.shopzone.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for an order item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

  private String id;
  private String productId;
  private String productName;
  private String productSlug;
  private String productSku;
  private String productImage;
  private String productBrand;

  private BigDecimal unitPrice;
  private BigDecimal discountPrice;
  private BigDecimal effectivePrice;
  private Integer quantity;
  private BigDecimal totalPrice;

  private boolean hasDiscount;
  private Integer discountPercentage;
  private BigDecimal savings;

  /**
   * Create response from OrderItem entity.
   */
  public static OrderItemResponse fromEntity(OrderItem item) {
    return OrderItemResponse.builder()
        .id(item.getId())
        .productId(item.getProductId())
        .productName(item.getProductName())
        .productSlug(item.getProductSlug())
        .productSku(item.getProductSku())
        .productImage(item.getProductImage())
        .productBrand(item.getProductBrand())
        .unitPrice(item.getUnitPrice())
        .discountPrice(item.getDiscountPrice())
        .effectivePrice(item.getEffectivePrice())
        .quantity(item.getQuantity())
        .totalPrice(item.getTotalPrice())
        .hasDiscount(item.hasDiscount())
        .discountPercentage(item.getDiscountPercentage())
        .savings(item.getSavings())
        .build();
  }
}