package com.shopzone.service;

import com.shopzone.dto.request.LoginRequest;
import com.shopzone.dto.request.RegisterRequest;
import com.shopzone.dto.response.AuthResponse;
import com.shopzone.exception.BadRequestException;
import com.shopzone.exception.UnauthorizedException;
import com.shopzone.model.Role;
import com.shopzone.model.User;
import com.shopzone.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtService jwtService;

  @Mock
  private AuthenticationManager authenticationManager;

  @InjectMocks
  private AuthService authService;

  private RegisterRequest registerRequest;
  private LoginRequest loginRequest;
  private User user;

  @BeforeEach
  void setUp() {
    registerRequest = RegisterRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email("john@example.com")
        .password("SecurePass123!")
        .phone("1234567890")
        .build();

    loginRequest = LoginRequest.builder()
        .email("john@example.com")
        .password("SecurePass123!")
        .build();

    user = User.builder()
        .id(UUID.randomUUID())
        .firstName("John")
        .lastName("Doe")
        .email("john@example.com")
        .password("encodedPassword")
        .role(Role.CUSTOMER)
        .emailVerified(false)
        .enabled(true)
        .locked(false)
        .build();
  }

  @Test
  @DisplayName("Should register user successfully")
  void register_WithValidRequest_ReturnsAuthResponse() {
    // Given
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(jwtService.generateToken(any(User.class))).thenReturn("accessToken");
    when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
    when(jwtService.getJwtExpiration()).thenReturn(86400000L);

    // When
    AuthResponse response = authService.register(registerRequest);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getAccessToken()).isEqualTo("accessToken");
    assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
    assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");

    verify(userRepository).existsByEmail("john@example.com");
    verify(userRepository, times(2)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw exception when email already exists")
  void register_WithExistingEmail_ThrowsBadRequestException() {
    // Given
    when(userRepository.existsByEmail(anyString())).thenReturn(true);

    // When/Then
    assertThatThrownBy(() -> authService.register(registerRequest))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Email already registered");

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should login successfully")
  void login_WithValidCredentials_ReturnsAuthResponse() {
    // Given
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(jwtService.generateToken(any(User.class))).thenReturn("accessToken");
    when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
    when(jwtService.getJwtExpiration()).thenReturn(86400000L);

    // When
    AuthResponse response = authService.login(loginRequest);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getAccessToken()).isEqualTo("accessToken");

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  @DisplayName("Should throw exception for invalid credentials")
  void login_WithInvalidCredentials_ThrowsUnauthorizedException() {
    // Given
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    // When/Then
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage("Invalid email or password");
  }

  @Test
  @DisplayName("Should throw exception for locked account")
  void login_WithLockedAccount_ThrowsUnauthorizedException() {
    // Given
    user.setLocked(true);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

    // When/Then
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage("Account is locked. Please contact support.");
  }

  @Test
  @DisplayName("Should verify email successfully")
  void verifyEmail_WithValidToken_VerifiesUser() {
    // Given
    user.setVerificationToken("valid-token");
    user.setVerificationTokenExpiry(java.time.LocalDateTime.now().plusHours(1));
    when(userRepository.findByVerificationToken("valid-token")).thenReturn(Optional.of(user));

    // When
    authService.verifyEmail("valid-token");

    // Then
    assertThat(user.getEmailVerified()).isTrue();
    assertThat(user.getVerificationToken()).isNull();
    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("Should throw exception for invalid verification token")
  void verifyEmail_WithInvalidToken_ThrowsBadRequestException() {
    // Given
    when(userRepository.findByVerificationToken("invalid-token")).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> authService.verifyEmail("invalid-token"))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Invalid verification token");
  }
}