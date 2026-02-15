package com.shopzone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "product_id"},
        name = "uk_user_product_review"
    ),
    indexes = {
        @Index(name = "idx_review_product", columnList = "product_id"),
        @Index(name = "idx_review_user", columnList = "user_id"),
        @Index(name = "idx_review_rating", columnList = "rating"),
        @Index(name = "idx_review_created", columnList = "created_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "product_id", nullable = false)
  private String productId;

  @NotNull(message = "Rating is required")
  @Min(value = 1, message = "Rating must be at least 1")
  @Max(value = 5, message = "Rating must be at most 5")
  @Column(nullable = false)
  private Integer rating;

  @Column(length = 100)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String comment;

  @Column(name = "verified_purchase", nullable = false)
  @Builder.Default
  private Boolean verifiedPurchase = false;

  @Column(name = "helpful_count", nullable = false)
  @Builder.Default
  private Integer helpfulCount = 0;

  @Column(name = "order_number")
  private String orderNumber;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public String getUserName() {
    return user != null ? user.getFirstName() + " " + user.getLastName().charAt(0) + "." : "Anonymous";
  }

  public String getMaskedUserName() {
    if (user == null) return "Anonymous";
    String firstName = user.getFirstName();
    if (firstName.length() <= 2) return firstName + " " + user.getLastName().charAt(0) + ".";
    return firstName.charAt(0) + "***" + firstName.charAt(firstName.length() - 1) + " " + user.getLastName().charAt(0) + ".";
  }
}