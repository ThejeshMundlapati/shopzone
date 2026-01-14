package com.shopzone.controller;

import com.shopzone.dto.request.*;
import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.response.AuthResponse;
import com.shopzone.dto.response.UserResponse;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.User;
import com.shopzone.repository.jpa.UserRepository;
import com.shopzone.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;
  private final UserRepository userRepository;

  public AuthController(AuthService authService, UserRepository userRepository) {
    this.authService = authService;
    this.userRepository = userRepository;
  }

  @Operation(
      summary = "Register a new user",
      description = "Creates a new user account and returns authentication tokens"
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "201",
          description = "User registered successfully",
          content = @Content(schema = @Schema(implementation = AuthResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "Invalid input or email already exists"
      )
  })
  @PostMapping("/register")
  @SecurityRequirements()
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    log.info("Registration request for email: {}", request.getEmail());
    AuthResponse response = authService.register(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("User registered successfully", response));
  }

  @Operation(
      summary = "User login",
      description = "Authenticates user and returns access and refresh tokens"
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Login successful"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "Invalid credentials"
      )
  })
  @PostMapping("/login")
  @SecurityRequirements()
  public ResponseEntity<ApiResponse<AuthResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    log.info("Login request for email: {}", request.getEmail());
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(ApiResponse.success("Login successful", response));
  }

  @Operation(
      summary = "Refresh access token",
      description = "Generates a new access token using the refresh token"
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Token refreshed successfully"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "Invalid or expired refresh token"
      )
  })
  @PostMapping("/refresh")
  @SecurityRequirements()
  public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {
    log.info("Token refresh request");
    AuthResponse response = authService.refreshToken(request);
    return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
  }

  @Operation(
      summary = "Request password reset",
      description = "Sends a password reset link to the user's email"
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Password reset email sent (if email exists)"
      )
  })
  @PostMapping("/forgot-password")
  @SecurityRequirements()
  public ResponseEntity<ApiResponse<Void>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    log.info("Password reset request for email: {}", request.getEmail());
    authService.forgotPassword(request);
    return ResponseEntity.ok(
        ApiResponse.success("If the email exists, a password reset link has been sent")
    );
  }

  @Operation(
      summary = "Reset password",
      description = "Resets the user's password using the reset token"
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Password reset successful"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "Invalid or expired token"
      )
  })
  @PostMapping("/reset-password")
  @SecurityRequirements()
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    log.info("Password reset with token");
    authService.resetPassword(request);
    return ResponseEntity.ok(ApiResponse.success("Password reset successful"));
  }

  @Operation(
      summary = "Verify email",
      description = "Verifies the user's email address using the verification token"
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Email verified successfully"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "Invalid or expired token"
      )
  })
  @GetMapping("/verify/{token}")
  @SecurityRequirements()
  public ResponseEntity<ApiResponse<Void>> verifyEmail(@PathVariable String token) {
    log.info("Email verification request");
    authService.verifyEmail(token);
    return ResponseEntity.ok(ApiResponse.success("Email verified successfully"));
  }

  @Operation(
      summary = "User logout",
      description = "Logs out the current user and invalidates their refresh token"
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Logout successful"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "Not authenticated"
      )
  })
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @AuthenticationPrincipal UserDetails userDetails) {
    String email = userDetails.getUsername();
    log.info("Logout request for user: {}", email);
    authService.logout(email);
    return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
  }

  @Operation(
      summary = "Get current user",
      description = "Returns the currently authenticated user's information"
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "User information retrieved"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "Not authenticated"
      )
  })
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
      @AuthenticationPrincipal UserDetails userDetails) {
    String email = userDetails.getUsername();
    log.info("Get current user request for: {}", email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return ResponseEntity.ok(
        ApiResponse.success("User retrieved successfully", UserResponse.from(user))
    );
  }
}