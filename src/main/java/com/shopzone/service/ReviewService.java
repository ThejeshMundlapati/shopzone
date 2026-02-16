package com.shopzone.service;

import com.shopzone.dto.request.CreateReviewRequest;
import com.shopzone.dto.request.UpdateReviewRequest;
import com.shopzone.dto.response.ReviewResponse;
import com.shopzone.dto.response.ReviewStatsResponse;
import com.shopzone.exception.BadRequestException;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.Order;
import com.shopzone.model.Review;
import com.shopzone.model.User;
import com.shopzone.model.enums.OrderStatus;
import com.shopzone.model.Product;
import com.shopzone.repository.jpa.OrderRepository;
import com.shopzone.repository.jpa.ReviewRepository;
import com.shopzone.repository.mongo.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;
  private final ProductSyncService productSyncService;

  @Transactional
  public ReviewResponse createReview(User user, CreateReviewRequest request) {
    String productId = request.getProductId();

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

    if (reviewRepository.existsByUserAndProductId(user, productId)) {
      throw new BadRequestException("You have already reviewed this product");
    }

    VerificationResult verification = verifyPurchase(user, productId);

    Review review = Review.builder()
        .user(user)
        .productId(productId)
        .rating(request.getRating())
        .title(request.getTitle())
        .comment(request.getComment())
        .verifiedPurchase(verification.verified)
        .orderNumber(verification.orderNumber)
        .build();

    review = reviewRepository.save(review);
    log.info("Review created by user {} for product {} (verified: {})",
        user.getId(), productId, verification.verified);

    updateProductRating(productId);

    return ReviewResponse.from(review, true);
  }

  @Transactional
  public ReviewResponse updateReview(User user, UUID reviewId, UpdateReviewRequest request) {
    Review review = reviewRepository.findByIdAndUser(reviewId, user)
        .orElseThrow(() -> new ResourceNotFoundException("Review not found or not owned by user"));

    if (request.getRating() != null) {
      review.setRating(request.getRating());
    }
    if (request.getTitle() != null) {
      review.setTitle(request.getTitle());
    }
    if (request.getComment() != null) {
      review.setComment(request.getComment());
    }

    review = reviewRepository.save(review);
    log.info("Review {} updated by user {}", reviewId, user.getId());

    updateProductRating(review.getProductId());

    return ReviewResponse.from(review, true);
  }

  @Transactional
  public void deleteReview(User user, UUID reviewId) {
    Review review = reviewRepository.findByIdAndUser(reviewId, user)
        .orElseThrow(() -> new ResourceNotFoundException("Review not found or not owned by user"));

    String productId = review.getProductId();
    reviewRepository.delete(review);
    log.info("Review {} deleted by user {}", reviewId, user.getId());

    updateProductRating(productId);
  }

  @Transactional
  public void adminDeleteReview(UUID reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

    String productId = review.getProductId();
    reviewRepository.delete(review);
    log.info("Review {} deleted by admin", reviewId);

    updateProductRating(productId);
  }

  @Transactional(readOnly = true)
  public Page<ReviewResponse> getProductReviews(String productId, String userId, Pageable pageable) {
    return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
        .map(review -> {
          boolean isOwner = userId != null &&
              review.getUser().getId().toString().equals(userId);
          return ReviewResponse.from(review, isOwner);
        });
  }

  @Transactional(readOnly = true)
  public Page<ReviewResponse> getProductReviewsByRating(String productId, Integer rating,
                                                        String userId, Pageable pageable) {
    return reviewRepository.findByProductIdAndRatingOrderByCreatedAtDesc(productId, rating, pageable)
        .map(review -> {
          boolean isOwner = userId != null &&
              review.getUser().getId().toString().equals(userId);
          return ReviewResponse.from(review, isOwner);
        });
  }

  @Transactional(readOnly = true)
  public Page<ReviewResponse> getVerifiedReviews(String productId, String userId, Pageable pageable) {
    return reviewRepository.findByProductIdAndVerifiedPurchaseTrueOrderByCreatedAtDesc(productId, pageable)
        .map(review -> {
          boolean isOwner = userId != null &&
              review.getUser().getId().toString().equals(userId);
          return ReviewResponse.from(review, isOwner);
        });
  }

  @Transactional(readOnly = true)
  public Page<ReviewResponse> getUserReviews(User user, Pageable pageable) {
    return reviewRepository.findByUserOrderByCreatedAtDesc(user, pageable)
        .map(review -> ReviewResponse.from(review, true));
  }

  @Transactional(readOnly = true)
  public ReviewStatsResponse getProductReviewStats(String productId) {
    Double avgRating = reviewRepository.findAverageRatingByProductId(productId);
    Long totalReviews = reviewRepository.countByProductId(productId);
    Long verifiedCount = reviewRepository.countVerifiedByProductId(productId);
    List<Object[]> distribution = reviewRepository.findRatingDistributionByProductId(productId);

    Map<Integer, Integer> ratingDistribution = new HashMap<>();
    Map<Integer, Double> ratingPercentages = new HashMap<>();

    for (int i = 1; i <= 5; i++) {
      ratingDistribution.put(i, 0);
      ratingPercentages.put(i, 0.0);
    }

    for (Object[] row : distribution) {
      Integer rating = (Integer) row[0];
      Long count = (Long) row[1];
      ratingDistribution.put(rating, count.intValue());
      if (totalReviews > 0) {
        ratingPercentages.put(rating, (count * 100.0) / totalReviews);
      }
    }

    return ReviewStatsResponse.builder()
        .productId(productId)
        .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
        .totalReviews(totalReviews != null ? totalReviews.intValue() : 0)
        .ratingDistribution(ratingDistribution)
        .ratingPercentages(ratingPercentages)
        .verifiedPurchaseCount(verifiedCount != null ? verifiedCount.intValue() : 0)
        .build();
  }

  @Transactional
  public void markHelpful(UUID reviewId) {
    if (!reviewRepository.existsById(reviewId)) {
      throw new ResourceNotFoundException("Review not found");
    }
    reviewRepository.incrementHelpfulCount(reviewId);
  }

  @Transactional(readOnly = true)
  public Optional<ReviewResponse> getUserReviewForProduct(User user, String productId) {
    return reviewRepository.findByUserAndProductId(user, productId)
        .map(review -> ReviewResponse.from(review, true));
  }

  @Transactional(readOnly = true)
  public boolean canUserReview(User user, String productId) {
    if (reviewRepository.existsByUserAndProductId(user, productId)) {
      return false;
    }
    return true;
  }


  private VerificationResult verifyPurchase(User user, String productId) {
    List<Order> deliveredOrders = orderRepository.findByUserIdAndStatus(
        user.getId().toString(),
        OrderStatus.DELIVERED
    );

    for (Order order : deliveredOrders) {
      boolean hasProduct = order.getItems().stream()
          .anyMatch(item -> item.getProductId().equals(productId));
      if (hasProduct) {
        return new VerificationResult(true, order.getOrderNumber());
      }
    }

    return new VerificationResult(false, null);
  }

  private void updateProductRating(String productId) {
    try {
      Double avgRating = reviewRepository.findAverageRatingByProductId(productId);
      Long reviewCount = reviewRepository.countByProductId(productId);

      productSyncService.updateProductRating(
          productId,
          avgRating != null ? avgRating : 0.0,
          reviewCount != null ? reviewCount.intValue() : 0
      );
    } catch (Exception e) {
      log.error("Failed to update product rating in Elasticsearch: {}", e.getMessage());
    }
  }

  private record VerificationResult(boolean verified, String orderNumber) {}
}