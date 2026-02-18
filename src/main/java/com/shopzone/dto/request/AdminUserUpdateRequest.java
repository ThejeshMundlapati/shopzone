package com.shopzone.dto.request;

import com.shopzone.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for admin user updates.
 * All fields are optional - only provided fields are updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateRequest {

  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private Role role;
  private Boolean enabled;
  private Boolean locked;
  private Boolean emailVerified;
}