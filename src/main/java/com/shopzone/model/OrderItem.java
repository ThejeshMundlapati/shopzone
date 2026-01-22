package com.shopzone.model;

import com.shopzone.dto.response.CartItemResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(name = "product_id", nullable = false)
  private String productId;

  @Column(name = "product_name", nullable = false)
  private String productName;

  @Column(name = "product_slug")
  private String productSlug;

  @Column(name = "product_sku")
  private String productSku;

  @Column(name = "product_image")
  private String productImage;

  @Column(name = "product_brand")
  private String productBrand;

  @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
  private BigDecimal unitPrice;

  @Column(name = "discount_price", precision = 10, scale = 2)
  private BigDecimal discountPrice;

  @Column(name = "effective_price", precision = 10, scale = 2, nullable = false)
  private BigDecimal effectivePrice;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
  private BigDecimal totalPrice;


  public BigDecimal getSavings() {
    if (hasDiscount()) {
      BigDecimal savingsPerUnit = unitPrice.subtract(discountPrice);
      return savingsPerUnit.multiply(BigDecimal.valueOf(quantity));
    }
    return BigDecimal.ZERO;
  }

  public boolean hasDiscount() {
    return discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0 && discountPrice.compareTo(unitPrice) < 0;
  }

  public Integer getDiscountPercentage() {
    if (!hasDiscount()) return 0;
    BigDecimal discount = unitPrice.subtract(discountPrice);
    return discount.multiply(BigDecimal.valueOf(100))
        .divide(unitPrice, 0, RoundingMode.HALF_UP)
        .intValue();
  }


  public static OrderItem fromCartItem(CartItem cartItem, Product product) {
    BigDecimal effectivePrice = product.getDiscountPrice() != null
        ? product.getDiscountPrice()
        : product.getPrice();

    return OrderItem.builder()
        .productId(product.getId())
        .productName(product.getName())
        .productSlug(product.getSlug())
        .productSku(product.getSku())
        .productImage(product.getImages() != null && !product.getImages().isEmpty()
            ? product.getImages().get(0)
            : null)
        .productBrand(product.getBrand())
        .unitPrice(product.getPrice())
        .discountPrice(product.getDiscountPrice())
        .effectivePrice(effectivePrice)
        .quantity(cartItem.getQuantity())
        .totalPrice(effectivePrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())))
        .build();
  }

  public static OrderItem fromCartItem(CartItemResponse cartItem, Product product) {
    BigDecimal effectivePrice = product.getDiscountPrice() != null
        ? product.getDiscountPrice()
        : product.getPrice();

    return OrderItem.builder()
        .productId(product.getId())
        .productName(product.getName())
        .productSlug(product.getSlug())
        .productSku(product.getSku())
        .productImage(product.getImages() != null && !product.getImages().isEmpty()
            ? product.getImages().get(0)
            : null)
        .productBrand(product.getBrand())
        .unitPrice(product.getPrice())
        .discountPrice(product.getDiscountPrice())
        .effectivePrice(effectivePrice)
        .quantity(cartItem.getQuantity())
        .totalPrice(effectivePrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())))
        .build();
  }
}