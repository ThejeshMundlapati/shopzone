package com.shopzone.productservice.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CategoryResponse {
    private String id, name, description, slug, imageUrl, parentId, parentName;
    private boolean active;
    private int displayOrder, productCount;
    private LocalDateTime createdAt, updatedAt;
    private List<CategoryResponse> children;
}
