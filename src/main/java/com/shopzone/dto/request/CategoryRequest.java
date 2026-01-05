package com.shopzone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

  @NotBlank(message = "Category name is required")
  @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
  private String name;

  @Size(max = 500, message = "Description must not exceed 500 characters")
  private String description;

  private String slug;  // Optional - will be auto-generated from name if not provided

  private String imageUrl;

  private String parentId;

  @Builder.Default
  private boolean active = true;

  @Builder.Default
  private int displayOrder = 0;
}
