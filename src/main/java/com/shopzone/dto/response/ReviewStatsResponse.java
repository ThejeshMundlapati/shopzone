package com.shopzone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsResponse {

  private String productId;
  private Double averageRating;
  private Integer totalReviews;
  private Map<Integer, Integer> ratingDistribution;
  private Map<Integer, Double> ratingPercentages;
  private Integer verifiedPurchaseCount;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RatingBreakdown {
    private Integer rating;
    private Integer count;
    private Double percentage;
  }
}