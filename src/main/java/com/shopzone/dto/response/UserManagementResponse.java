package com.shopzone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shopzone.model.Role;
import com.shopzone.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for admin user management.
 * Includes user details plus order/review statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserManagementResponse {

  private UUID id;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private Role role;
  private Boolean emailVerified;
  private Boolean enabled;
  private Boolean locked;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private Long orderCount;
  private BigDecimal totalSpent;
  private Long reviewCount;
  private LocalDateTime lastOrderAt;

  /**
   * Create from User entity (without stats).
   */
  public static UserManagementResponse fromEntity(User user) {
    return UserManagementResponse.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .phone(user.getPhone())
        .role(user.getRole())
        .emailVerified(user.getEmailVerified())
        .enabled(user.getEnabled())
        .locked(user.getLocked())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }

  /**
   * Create from User entity with order/review statistics.
   */
  public static UserManagementResponse fromEntityWithStats(
      User user, long orderCount, BigDecimal totalSpent,
      long reviewCount, LocalDateTime lastOrderAt) {

    return UserManagementResponse.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .phone(user.getPhone())
        .role(user.getRole())
        .emailVerified(user.getEmailVerified())
        .enabled(user.getEnabled())
        .locked(user.getLocked())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .orderCount(orderCount)
        .totalSpent(totalSpent)
        .reviewCount(reviewCount)
        .lastOrderAt(lastOrderAt)
        .build();
  }
}