package com.shopzone.cartservice.model;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WishlistItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String productId, productName, productSlug, imageUrl;
    private BigDecimal price, discountPrice;
    private boolean inStock;
    @Builder.Default private LocalDateTime addedAt = LocalDateTime.now();
}
