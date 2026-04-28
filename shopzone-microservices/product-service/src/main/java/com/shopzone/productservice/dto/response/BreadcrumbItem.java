package com.shopzone.productservice.dto.response;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BreadcrumbItem {
    private String id, name, slug;
    private int level;
}
