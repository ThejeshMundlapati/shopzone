//package com.shopzone.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.shopzone.dto.request.LoginRequest;
//import com.shopzone.dto.request.RegisterRequest;
//import com.shopzone.dto.response.AuthResponse;
//import com.shopzone.dto.response.UserResponse;
//import com.shopzone.model.Role;
//import com.shopzone.security.JwtAuthenticationFilter;
//import com.shopzone.service.AuthService;
//import com.shopzone.service.JwtService;
//import com.shopzone.service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(AuthController.class)
//@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
//class AuthControllerTest {
//
//  @Autowired
//  private MockMvc mockMvc;
//
//  @Autowired
//  private ObjectMapper objectMapper;
//
//  @MockitoBean
//  private AuthService authService;
//
//  @MockitoBean
//  private JwtService jwtService;
//
//  @MockitoBean
//  private UserService userService;
//
//  @MockitoBean
//  private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//  private RegisterRequest validRegisterRequest;
//  private LoginRequest validLoginRequest;
//  private AuthResponse authResponse;
//
//  @BeforeEach
//  void setUp() {
//    validRegisterRequest = RegisterRequest.builder()
//        .firstName("John")
//        .lastName("Doe")
//        .email("john@example.com")
//        .password("SecurePass123!")
//        .phone("1234567890")
//        .build();
//
//    validLoginRequest = LoginRequest.builder()
//        .email("john@example.com")
//        .password("SecurePass123!")
//        .build();
//
//    UserResponse userResponse = UserResponse.builder()
//        .id(UUID.randomUUID())
//        .firstName("John")
//        .lastName("Doe")
//        .email("john@example.com")
//        .role(Role.CUSTOMER)
//        .emailVerified(false)
//        .createdAt(LocalDateTime.now())
//        .build();
//
//    authResponse = AuthResponse.builder()
//        .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
//        .refreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
//        .tokenType("Bearer")
//        .expiresIn(86400000L)
//        .user(userResponse)
//        .build();
//  }
//
//  @Test
//  @DisplayName("Should register user successfully")
//  void register_WithValidRequest_ReturnsCreated() throws Exception {
//    when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);
//
//    mockMvc.perform(post("/api/auth/register")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(validRegisterRequest)))
//        .andExpect(status().isCreated())
//        .andExpect(jsonPath("$.success").value(true))
//        .andExpect(jsonPath("$.message").value("User registered successfully"))
//        .andExpect(jsonPath("$.data.accessToken").exists())
//        .andExpect(jsonPath("$.data.user.email").value("john@example.com"));
//  }
//
//  @Test
//  @DisplayName("Should return 400 for invalid email")
//  void register_WithInvalidEmail_ReturnsBadRequest() throws Exception {
//    validRegisterRequest.setEmail("invalid-email");
//
//    mockMvc.perform(post("/api/auth/register")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(validRegisterRequest)))
//        .andExpect(status().isBadRequest());
//  }
//
//  @Test
//  @DisplayName("Should return 400 for weak password")
//  void register_WithWeakPassword_ReturnsBadRequest() throws Exception {
//    validRegisterRequest.setPassword("weak");
//
//    mockMvc.perform(post("/api/auth/register")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(validRegisterRequest)))
//        .andExpect(status().isBadRequest());
//  }
//
//  @Test
//  @DisplayName("Should login successfully")
//  void login_WithValidCredentials_ReturnsOk() throws Exception {
//    when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);
//
//    mockMvc.perform(post("/api/auth/login")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(validLoginRequest)))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.success").value(true))
//        .andExpect(jsonPath("$.message").value("Login successful"))
//        .andExpect(jsonPath("$.data.accessToken").exists());
//  }
//
//  @Test
//  @DisplayName("Should return 400 for empty email on login")
//  void login_WithEmptyEmail_ReturnsBadRequest() throws Exception {
//    validLoginRequest.setEmail("");
//
//    mockMvc.perform(post("/api/auth/login")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(validLoginRequest)))
//        .andExpect(status().isBadRequest());
//  }
//}