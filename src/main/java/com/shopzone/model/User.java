package com.shopzone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank(message = "First name is required")
  @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
  @Column(name = "first_name", nullable = false, length = 50)
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
  @Column(name = "last_name", nullable = false, length = 50)
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Please provide a valid email address")
  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  @Column(nullable = false)
  private String password;

  @Size(max = 15, message = "Phone number cannot exceed 15 characters")
  @Column(length = 15)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private Role role = Role.CUSTOMER;

  @Column(name = "email_verified")
  @Builder.Default
  private Boolean emailVerified = false;

  @Column(name = "verification_token")
  private String verificationToken;

  @Column(name = "verification_token_expiry")
  private LocalDateTime verificationTokenExpiry;

  @Column(name = "password_reset_token")
  private String passwordResetToken;

  @Column(name = "password_reset_token_expiry")
  private LocalDateTime passwordResetTokenExpiry;

  @Column(name = "refresh_token")
  private String refreshToken;

  @Builder.Default
  private Boolean enabled = true;

  @Builder.Default
  private Boolean locked = false;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // UserDetails implementation
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return !locked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  // Helper method
  public String getFullName() {
    return firstName + " " + lastName;
  }
}