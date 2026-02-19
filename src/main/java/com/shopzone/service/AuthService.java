package com.shopzone.service;

import com.shopzone.dto.request.*;
import com.shopzone.dto.response.AuthResponse;
import com.shopzone.dto.response.UserResponse;
import com.shopzone.exception.BadRequestException;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.exception.UnauthorizedException;
import com.shopzone.model.User;
import com.shopzone.repository.jpa.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final UserService userService;

  public AuthService(UserRepository userRepository,
                     PasswordEncoder passwordEncoder,
                     JwtService jwtService,
                     @Lazy AuthenticationManager authenticationManager, UserService userService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
    this.userService = userService;
  }

  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
      throw new BadRequestException("Email already registered");
    }

    String verificationToken = UUID.randomUUID().toString();

    User user = User.builder()
        .firstName(request.getFirstName().trim())
        .lastName(request.getLastName().trim())
        .email(request.getEmail().toLowerCase().trim())
        .password(passwordEncoder.encode(request.getPassword()))
        .phone(request.getPhone())
        .verificationToken(verificationToken)
        .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
        .build();

    User savedUser = userRepository.save(user);
    log.info("User registered successfully: {}", savedUser.getEmail());

    userService.sendWelcomeEmail(user);

    String accessToken = jwtService.generateToken(savedUser);
    String refreshToken = jwtService.generateRefreshToken(savedUser);

    savedUser.setRefreshToken(refreshToken);
    userRepository.save(savedUser);


    log.info("Verification token for {}: {}", savedUser.getEmail(), verificationToken);

    return AuthResponse.of(
        accessToken,
        refreshToken,
        jwtService.getJwtExpiration(),
        UserResponse.from(savedUser)
    );
  }

  public AuthResponse login(LoginRequest request) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              request.getEmail().toLowerCase(),
              request.getPassword()
          )
      );
    } catch (AuthenticationException e) {
      throw new UnauthorizedException("Invalid email or password");
    }

    User user = userRepository.findByEmail(request.getEmail().toLowerCase())
        .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

    if (user.getLocked()) {
      throw new UnauthorizedException("Account is locked. Please contact support.");
    }

    String accessToken = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    user.setRefreshToken(refreshToken);
    userRepository.save(user);

    log.info("User logged in successfully: {}", user.getEmail());

    return AuthResponse.of(
        accessToken,
        refreshToken,
        jwtService.getJwtExpiration(),
        UserResponse.from(user)
    );
  }

  public AuthResponse refreshToken(RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();

    if (!jwtService.validateToken(refreshToken)) {
      throw new UnauthorizedException("Invalid or expired refresh token");
    }

    String email = jwtService.extractUsername(refreshToken);
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UnauthorizedException("User not found"));

    if (!refreshToken.equals(user.getRefreshToken())) {
      throw new UnauthorizedException("Invalid refresh token");
    }

    String newAccessToken = jwtService.generateToken(user);
    String newRefreshToken = jwtService.generateRefreshToken(user);

    user.setRefreshToken(newRefreshToken);
    userRepository.save(user);

    log.info("Token refreshed for user: {}", user.getEmail());

    return AuthResponse.of(
        newAccessToken,
        newRefreshToken,
        jwtService.getJwtExpiration(),
        UserResponse.from(user)
    );
  }

  public void forgotPassword(ForgotPasswordRequest request) {
    User user = userRepository.findByEmail(request.getEmail().toLowerCase())
        .orElse(null);

    if (user == null) {
      log.warn("Password reset requested for non-existent email: {}", request.getEmail());
      return;
    }

    String resetToken = UUID.randomUUID().toString();
    user.setPasswordResetToken(resetToken);
    user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
    userRepository.save(user);

    userService.sendPasswordResetEmail(user, resetToken);


    log.info("Password reset token for {}: {}", user.getEmail(), resetToken);
  }

  public void resetPassword(ResetPasswordRequest request) {
    User user = userRepository.findByPasswordResetToken(request.getToken())
        .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

    if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
      throw new BadRequestException("Reset token has expired");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    user.setPasswordResetToken(null);
    user.setPasswordResetTokenExpiry(null);
    user.setRefreshToken(null);
    userRepository.save(user);

    log.info("Password reset successfully for user: {}", user.getEmail());
  }

  public void verifyEmail(String token) {
    User user = userRepository.findByVerificationToken(token)
        .orElseThrow(() -> new BadRequestException("Invalid verification token"));

    if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
      throw new BadRequestException("Verification token has expired");
    }

    user.setEmailVerified(true);
    user.setVerificationToken(null);
    user.setVerificationTokenExpiry(null);
    userRepository.save(user);

    log.info("Email verified for user: {}", user.getEmail());
  }

  public void logout(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.setRefreshToken(null);
    userRepository.save(user);

    log.info("User logged out: {}", email);
  }
}