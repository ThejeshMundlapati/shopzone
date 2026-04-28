package com.shopzone.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Shared product representation returned by Product Service.
 * Other services (Cart, Order, Review) use this via inter-service calls.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private String slug;
    private String sku;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer discountPercentage;
    private Integer stock;
    private boolean inStock;
    private String categoryId;
    private String categoryName;
    private String brand;
    private List<String> images;
    private List<String> tags;
    private boolean active;
    private boolean featured;
    private ProductDetailsResponse details;
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDetailsResponse {
        private String weight;
        private String dimensions;
        private String color;
        private String size;
        private String material;
        private Map<String, String> specifications;
    }

    /**
     * Get effective price (discount price if available, otherwise regular price).
     */
    public BigDecimal getEffectivePrice() {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0) {
            return discountPrice;
        }
        return price;
    }

    /**
     * Get first image URL.
     */
    public String getFirstImage() {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }
}
