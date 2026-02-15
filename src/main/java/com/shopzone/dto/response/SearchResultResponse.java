package com.shopzone.dto.response;

import com.shopzone.model.elasticsearch.ProductDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultResponse {

  private List<ProductHit> products;
  private long totalHits;
  private int totalPages;
  private int currentPage;
  private int pageSize;
  private String query;
  private Map<String, List<FacetValue>> facets;
  private long searchTimeMs;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProductHit {
    private String id;
    private String name;
    private String description;
    private String slug;
    private String brand;
    private Double price;
    private Double salePrice;
    private Integer stock;
    private String categoryId;
    private String categoryName;
    private List<String> tags;
    private List<String> images;
    private Double averageRating;
    private Integer reviewCount;
    private Double score;

    public static ProductHit from(ProductDocument doc, float score) {
      return ProductHit.builder()
          .id(doc.getId())
          .name(doc.getName())
          .description(truncateDescription(doc.getDescription()))
          .slug(doc.getSlug())
          .brand(doc.getBrand())
          .price(doc.getPrice())
          .salePrice(doc.getSalePrice())
          .stock(doc.getStock())
          .categoryId(doc.getCategoryId())
          .categoryName(doc.getCategoryName())
          .tags(doc.getTags())
          .images(doc.getImages())
          .averageRating(doc.getAverageRating())
          .reviewCount(doc.getReviewCount())
          .score((double) score)
          .build();
    }

    private static String truncateDescription(String desc) {
      if (desc == null) return null;
      return desc.length() > 200 ? desc.substring(0, 200) + "..." : desc;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FacetValue {
    private String value;
    private long count;
  }
}