package com.shopzone.productservice.dto.request;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductUpdateRequest {
    private String name, description, slug, sku, categoryId, brand;
    private BigDecimal price, discountPrice;
    private Integer stock;
    private List<String> tags;
    private Boolean active, featured;
    private ProductDetailsRequest details;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProductDetailsRequest {
        private String weight, dimensions, color, size, material;
        private Map<String, String> specifications;
    }
}
