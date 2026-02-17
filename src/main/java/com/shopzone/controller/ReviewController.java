package com.shopzone.controller;

import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.request.CreateReviewRequest;
import com.shopzone.dto.request.UpdateReviewRequest;
import com.shopzone.dto.response.ReviewResponse;
import com.shopzone.dto.response.ReviewStatsResponse;
import com.shopzone.model.User;
import com.shopzone.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product reviews and ratings")
public class ReviewController {

  private final ReviewService reviewService;


  @GetMapping("/product/{productId}")
  @Operation(summary = "Get reviews for a product")
  public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getProductReviews(
      @PathVariable String productId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) Integer rating,
      @RequestParam(required = false) Boolean verifiedOnly,
      @AuthenticationPrincipal User user) {

    Pageable pageable = PageRequest.of(page, size);
    String userId = user != null ? user.getId().toString() : null;

    Page<ReviewResponse> reviews;

    if (rating != null) {
      reviews = reviewService.getProductReviewsByRating(productId, rating, userId, pageable);
    } else if (Boolean.TRUE.equals(verifiedOnly)) {
      reviews = reviewService.getVerifiedReviews(productId, userId, pageable);
    } else {
      reviews = reviewService.getProductReviews(productId, userId, pageable);
    }

    return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", reviews));
  }

  @GetMapping("/product/{productId}/stats")
  @Operation(summary = "Get review statistics for a product")
  public ResponseEntity<ApiResponse<ReviewStatsResponse>> getProductReviewStats(
      @PathVariable String productId) {

    ReviewStatsResponse stats = reviewService.getProductReviewStats(productId);
    return ResponseEntity.ok(ApiResponse.success("Review statistics retrieved", stats));
  }


  @PostMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Create a review", security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody CreateReviewRequest request) {

    ReviewResponse review = reviewService.createReview(user, request);
    return ResponseEntity.ok(ApiResponse.success("Review created successfully", review));
  }

  @PutMapping("/{reviewId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Update your review", security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
      @AuthenticationPrincipal User user,
      @PathVariable UUID reviewId,
      @Valid @RequestBody UpdateReviewRequest request) {

    ReviewResponse review = reviewService.updateReview(user, reviewId, request);
    return ResponseEntity.ok(ApiResponse.success("Review updated successfully", review));
  }

  @DeleteMapping("/{reviewId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Delete your review", security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<Void>> deleteReview(
      @AuthenticationPrincipal User user,
      @PathVariable UUID reviewId) {

    reviewService.deleteReview(user, reviewId);
    return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
  }

  @GetMapping("/my-reviews")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get your reviews", security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getMyReviews(
      @AuthenticationPrincipal User user,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<ReviewResponse> reviews = reviewService.getUserReviews(user, pageable);
    return ResponseEntity.ok(ApiResponse.success("Your reviews retrieved", reviews));
  }

  @GetMapping("/product/{productId}/my-review")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get your review for a product", security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<ReviewResponse>> getMyReviewForProduct(
      @AuthenticationPrincipal User user,
      @PathVariable String productId) {

    return reviewService.getUserReviewForProduct(user, productId)
        .map(review -> ResponseEntity.ok(ApiResponse.success("Your review retrieved", review)))
        .orElse(ResponseEntity.ok(ApiResponse.success("No review found", null)));
  }

  @GetMapping("/product/{productId}/can-review")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Check if you can review a product", security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<Map<String, Boolean>>> canReviewProduct(
      @AuthenticationPrincipal User user,
      @PathVariable String productId) {

    boolean canReview = reviewService.canUserReview(user, productId);
    return ResponseEntity.ok(ApiResponse.success("Review eligibility checked",
        Map.of("canReview", canReview)));
  }

  @PostMapping("/{reviewId}/helpful")
  @Operation(summary = "Mark a review as helpful")
  public ResponseEntity<ApiResponse<Void>> markHelpful(@PathVariable UUID reviewId) {
    reviewService.markHelpful(reviewId);
    return ResponseEntity.ok(ApiResponse.success("Review marked as helpful", null));
  }


  @DeleteMapping("/admin/{reviewId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Admin: Delete any review", security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<Void>> adminDeleteReview(@PathVariable UUID reviewId) {
    reviewService.adminDeleteReview(reviewId);
    return ResponseEntity.ok(ApiResponse.success("Review deleted by admin", null));
  }
}