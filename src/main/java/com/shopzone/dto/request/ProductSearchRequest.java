package com.shopzone.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {

  private String query;

  private Double minPrice;
  private Double maxPrice;
  private String categoryId;
  private String brand;
  private List<String> tags;
  private Double minRating;
  private Boolean inStock;

  @Builder.Default
  private Integer page = 0;

  @Builder.Default
  private Integer size = 12;

  @Builder.Default
  private String sortBy = "relevance";

  @Builder.Default
  private String sortDir = "desc";
}