package com.shopzone.orderservice.dto.response;
import com.shopzone.orderservice.model.OrderItem;
import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItemResponse {
    private String productId, productName, productSlug, productSku, productImage, productBrand;
    private BigDecimal unitPrice, discountPrice, effectivePrice, totalPrice, savings;
    private int quantity;

    public static OrderItemResponse fromEntity(OrderItem i) {
        return OrderItemResponse.builder()
            .productId(i.getProductId()).productName(i.getProductName()).productSlug(i.getProductSlug())
            .productSku(i.getProductSku()).productImage(i.getProductImage()).productBrand(i.getProductBrand())
            .unitPrice(i.getUnitPrice()).discountPrice(i.getDiscountPrice()).effectivePrice(i.getEffectivePrice())
            .totalPrice(i.getTotalPrice()).savings(i.getSavings()).quantity(i.getQuantity()).build();
    }
}
