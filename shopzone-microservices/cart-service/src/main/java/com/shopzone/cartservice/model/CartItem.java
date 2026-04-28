package com.shopzone.cartservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String productId, productName, productSlug, imageUrl;
    private BigDecimal price, discountPrice;
    private int quantity, availableStock;
    private LocalDateTime addedAt;

    @JsonIgnore
    public BigDecimal getEffectivePrice() {
        return discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0 ? discountPrice : price;
    }
    @JsonIgnore
    public BigDecimal getSubtotal() { return getEffectivePrice().multiply(BigDecimal.valueOf(quantity)); }
    @JsonIgnore
    public boolean isQuantityValid() { return quantity > 0 && quantity <= availableStock; }
    @JsonIgnore
    public BigDecimal getSavings() {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0)
            return price.subtract(discountPrice).multiply(BigDecimal.valueOf(quantity));
        return BigDecimal.ZERO;
    }
}