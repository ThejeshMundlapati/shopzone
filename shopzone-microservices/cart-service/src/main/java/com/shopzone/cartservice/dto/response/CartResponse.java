package com.shopzone.cartservice.dto.response;
import com.shopzone.cartservice.model.Cart;
import com.shopzone.cartservice.model.CartItem;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CartResponse {
    private String userId;
    private List<CartItemResponse> items;
    private int totalItems, uniqueItemCount;
    private BigDecimal subtotal, totalSavings;

    public static CartResponse fromCart(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(CartItemResponse::from).collect(Collectors.toList());
        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal savings = cart.getItems().stream().map(CartItem::getSavings).reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponse.builder().userId(cart.getUserId()).items(items)
            .totalItems(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum())
            .uniqueItemCount(cart.getUniqueItemCount()).subtotal(subtotal).totalSavings(savings).build();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CartItemResponse {
        private String productId, productName, productSlug, imageUrl;
        private BigDecimal price, discountPrice, effectivePrice, subtotal, savings;
        private int quantity, availableStock;
        public static CartItemResponse from(CartItem i) {
            return CartItemResponse.builder()
                .productId(i.getProductId()).productName(i.getProductName()).productSlug(i.getProductSlug())
                .imageUrl(i.getImageUrl()).price(i.getPrice()).discountPrice(i.getDiscountPrice())
                .effectivePrice(i.getEffectivePrice()).subtotal(i.getSubtotal()).savings(i.getSavings())
                .quantity(i.getQuantity()).availableStock(i.getAvailableStock()).build();
        }
    }
}
