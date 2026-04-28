package com.shopzone.productservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CategoryRequest {
    @NotBlank private String name;
    private String description;
    private String slug;
    private String imageUrl;
    private String parentId;
    @Builder.Default private boolean active = true;
    @Builder.Default private int displayOrder = 0;
}
