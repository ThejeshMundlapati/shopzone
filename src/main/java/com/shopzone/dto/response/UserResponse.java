package com.shopzone.dto.response;

import com.shopzone.model.Role;
import com.shopzone.model.User;
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
public class UserResponse {
  private UUID id;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private Role role;
  private Boolean emailVerified;
  private LocalDateTime createdAt;

  public static UserResponse from(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .phone(user.getPhone())
        .role(user.getRole())
        .emailVerified(user.getEmailVerified())
        .createdAt(user.getCreatedAt())
        .build();
  }
}