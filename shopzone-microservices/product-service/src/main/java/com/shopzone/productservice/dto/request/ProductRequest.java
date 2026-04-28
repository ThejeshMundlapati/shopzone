package com.shopzone.productservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductRequest {
    @NotBlank private String name;
    private String description;
    private String slug;
    private String sku;
    @NotNull @DecimalMin("0.01") private BigDecimal price;
    private BigDecimal discountPrice;
    @Min(0) private Integer stock;
    @NotBlank private String categoryId;
    private String brand;
    private List<String> tags;
    @Builder.Default private boolean active = true;
    @Builder.Default private boolean featured = false;
    private ProductDetailsRequest details;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProductDetailsRequest {
        private String weight, dimensions, color, size, material;
        private Map<String, String> specifications;
    }
}
