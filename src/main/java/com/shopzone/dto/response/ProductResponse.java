package com.shopzone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private Double averageRating;

  private Integer reviewCount;

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
}
