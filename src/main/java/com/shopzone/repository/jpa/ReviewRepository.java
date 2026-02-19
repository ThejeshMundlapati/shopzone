package com.shopzone.repository.jpa;

import com.shopzone.model.Review;
import com.shopzone.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

  Page<Review> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);

  List<Review> findByProductId(String productId);

  Page<Review> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

  List<Review> findByUser(User user);

  Optional<Review> findByUserAndProductId(User user, String productId);

  Optional<Review> findByIdAndUser(UUID id, User user);

  boolean existsByUserAndProductId(User user, String productId);

  @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
  Double findAverageRatingByProductId(@Param("productId") String productId);

  @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId")
  Long countByProductId(@Param("productId") String productId);

  @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.verifiedPurchase = true")
  Long countVerifiedByProductId(@Param("productId") String productId);

  @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = :productId GROUP BY r.rating ORDER BY r.rating DESC")
  List<Object[]> findRatingDistributionByProductId(@Param("productId") String productId);

  Page<Review> findByProductIdAndRatingOrderByCreatedAtDesc(String productId, Integer rating, Pageable pageable);

  Page<Review> findByProductIdAndVerifiedPurchaseTrueOrderByCreatedAtDesc(String productId, Pageable pageable);

  @Modifying
  @Query("UPDATE Review r SET r.helpfulCount = r.helpfulCount + 1 WHERE r.id = :reviewId")
  void incrementHelpfulCount(@Param("reviewId") UUID reviewId);

  @Modifying
  void deleteByProductId(String productId);

  Page<Review> findByProductIdOrderByHelpfulCountDescCreatedAtDesc(String productId, Pageable pageable);


  /**
   * Calculate average rating across all reviews (for dashboard)
   */
  @Query("SELECT AVG(r.rating) FROM Review r")
  Double findAverageRating();

  /**
   * Count reviews by user ID (for admin user stats).
   */
  long countByUserId(UUID userId);
}