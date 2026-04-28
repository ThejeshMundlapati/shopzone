package com.shopzone.orderservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(name = "product_id", nullable = false) private String productId;
    @Column(name = "product_name", nullable = false) private String productName;
    @Column(name = "product_slug") private String productSlug;
    @Column(name = "product_sku") private String productSku;
    @Column(name = "product_image") private String productImage;
    @Column(name = "product_brand") private String productBrand;
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false) private BigDecimal unitPrice;
    @Column(name = "discount_price", precision = 10, scale = 2) private BigDecimal discountPrice;
    @Column(name = "effective_price", precision = 10, scale = 2, nullable = false) private BigDecimal effectivePrice;
    @Column(nullable = false) private int quantity;
    @Column(name = "total_price", precision = 10, scale = 2, nullable = false) private BigDecimal totalPrice;

    public BigDecimal getSavings() {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0 && discountPrice.compareTo(unitPrice) < 0)
            return unitPrice.subtract(discountPrice).multiply(BigDecimal.valueOf(quantity));
        return BigDecimal.ZERO;
    }
}
