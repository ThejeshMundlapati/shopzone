package com.shopzone.dto.response;

import com.shopzone.model.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

  private UUID id;
  private String productId;
  private String userName;
  private Integer rating;
  private String title;
  private String comment;
  private Boolean verifiedPurchase;
  private Integer helpfulCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean isOwner;

  public static ReviewResponse from(Review review) {
    return from(review, false);
  }

  public static ReviewResponse from(Review review, boolean isOwner) {
    return ReviewResponse.builder()
        .id(review.getId())
        .productId(review.getProductId())
        .userName(review.getMaskedUserName())
        .rating(review.getRating())
        .title(review.getTitle())
        .comment(review.getComment())
        .verifiedPurchase(review.getVerifiedPurchase())
        .helpfulCount(review.getHelpfulCount())
        .createdAt(review.getCreatedAt())
        .updatedAt(review.getUpdatedAt())
        .isOwner(isOwner)
        .build();
  }
}