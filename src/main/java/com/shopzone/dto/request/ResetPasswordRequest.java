package com.shopzone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

  @NotBlank(message = "Token is required")
  private String token;

  @NotBlank(message = "New password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
      message = "Password must contain at least one digit, lowercase, uppercase, and special character"
  )
  private String newPassword;
}