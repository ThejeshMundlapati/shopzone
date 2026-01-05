package com.shopzone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

  private String id;
  private String name;
  private String description;
  private String slug;
  private String imageUrl;
  private String parentId;
  private String parentName;
  private boolean active;
  private int displayOrder;
  private int productCount;
  private List<CategoryResponse> children;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
