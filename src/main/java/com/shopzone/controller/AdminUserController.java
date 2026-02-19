package com.shopzone.controller;

import com.shopzone.dto.request.AdminUserUpdateRequest;
import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.response.UserManagementResponse;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.Role;
import com.shopzone.model.User;
import com.shopzone.repository.jpa.OrderRepository;
import com.shopzone.repository.jpa.ReviewRepository;
import com.shopzone.repository.jpa.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Admin User Management API.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin User Management", description = "Admin endpoints for managing users")
public class AdminUserController {

  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final ReviewRepository reviewRepository;


  @GetMapping
  @Operation(summary = "Get all users", description = "List all users with pagination and optional filtering")
  public ResponseEntity<ApiResponse<Page<UserManagementResponse>>> getAllUsers(
      @Parameter(description = "Page number (0-indexed)")
      @RequestParam(defaultValue = "0") int page,

      @Parameter(description = "Page size")
      @RequestParam(defaultValue = "20") int size,

      @Parameter(description = "Sort field", example = "createdAt")
      @RequestParam(defaultValue = "createdAt") String sortBy,

      @Parameter(description = "Sort direction", example = "desc")
      @RequestParam(defaultValue = "desc") String sortDir,

      @Parameter(description = "Filter by role")
      @RequestParam(required = false) Role role,

      @Parameter(description = "Search by email or name")
      @RequestParam(required = false) String search) {

    Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<User> users;
    if (role != null && search != null && !search.isEmpty()) {
      users = userRepository.findByRoleAndSearch(role, search, pageable);
    } else if (role != null) {
      users = userRepository.findByRole(role, pageable);
    } else if (search != null && !search.isEmpty()) {
      users = userRepository.searchUsers(search, pageable);
    } else {
      users = userRepository.findAll(pageable);
    }

    Page<UserManagementResponse> response = users.map(this::mapUserWithStats);
    return ResponseEntity.ok(ApiResponse.success("Users retrieved", response));
  }


  @GetMapping("/{userId}")
  @Operation(summary = "Get user details", description = "Get detailed information about a specific user")
  public ResponseEntity<ApiResponse<UserManagementResponse>> getUserDetails(
      @PathVariable String userId) {

    User user = userRepository.findById(UUID.fromString(userId))
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    UserManagementResponse response = mapUserWithStats(user);
    return ResponseEntity.ok(ApiResponse.success("User details retrieved", response));
  }


  @PutMapping("/{userId}")
  @Operation(summary = "Update user", description = "Update user information (admin only)")
  public ResponseEntity<ApiResponse<UserManagementResponse>> updateUser(
      @PathVariable String userId,
      @Valid @RequestBody AdminUserUpdateRequest request) {

    User user = userRepository.findById(UUID.fromString(userId))
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    if (request.getFirstName() != null) {
      user.setFirstName(request.getFirstName());
    }
    if (request.getLastName() != null) {
      user.setLastName(request.getLastName());
    }
    if (request.getEmail() != null) {
      if (!user.getEmail().equals(request.getEmail())
          && userRepository.existsByEmail(request.getEmail())) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Email already in use"));
      }
      user.setEmail(request.getEmail());
    }
    if (request.getPhone() != null) {
      user.setPhone(request.getPhone());
    }
    if (request.getRole() != null) {
      user.setRole(request.getRole());
    }
    if (request.getEnabled() != null) {
      user.setEnabled(request.getEnabled());
    }
    if (request.getLocked() != null) {
      user.setLocked(request.getLocked());
    }
    if (request.getEmailVerified() != null) {
      user.setEmailVerified(request.getEmailVerified());
    }

    user = userRepository.save(user);
    UserManagementResponse response = mapUserWithStats(user);
    return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
  }


  @PatchMapping("/{userId}/enable")
  @Operation(summary = "Enable user account")
  public ResponseEntity<ApiResponse<UserManagementResponse>> enableUser(@PathVariable String userId) {
    User user = userRepository.findById(UUID.fromString(userId))
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    user.setEnabled(true);
    user.setLocked(false);
    user = userRepository.save(user);

    return ResponseEntity.ok(ApiResponse.success("User enabled successfully",
        UserManagementResponse.fromEntity(user)));
  }

  @PatchMapping("/{userId}/disable")
  @Operation(summary = "Disable user account")
  public ResponseEntity<ApiResponse<UserManagementResponse>> disableUser(@PathVariable String userId) {
    User user = userRepository.findById(UUID.fromString(userId))
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    user.setEnabled(false);
    user = userRepository.save(user);

    return ResponseEntity.ok(ApiResponse.success("User disabled successfully",
        UserManagementResponse.fromEntity(user)));
  }

  @PatchMapping("/{userId}/lock")
  @Operation(summary = "Lock user account")
  public ResponseEntity<ApiResponse<UserManagementResponse>> lockUser(@PathVariable String userId) {
    User user = userRepository.findById(UUID.fromString(userId))
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    user.setLocked(true);
    user = userRepository.save(user);

    return ResponseEntity.ok(ApiResponse.success("User locked successfully",
        UserManagementResponse.fromEntity(user)));
  }

  @PatchMapping("/{userId}/unlock")
  @Operation(summary = "Unlock user account")
  public ResponseEntity<ApiResponse<UserManagementResponse>> unlockUser(@PathVariable String userId) {
    User user = userRepository.findById(UUID.fromString(userId))
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    user.setLocked(false);
    user = userRepository.save(user);

    return ResponseEntity.ok(ApiResponse.success("User unlocked successfully",
        UserManagementResponse.fromEntity(user)));
  }


  @PatchMapping("/{userId}/role")
  @Operation(summary = "Change user role")
  public ResponseEntity<ApiResponse<UserManagementResponse>> changeUserRole(
      @PathVariable String userId,
      @RequestParam Role role) {

    User user = userRepository.findById(UUID.fromString(userId))
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    user.setRole(role);
    user = userRepository.save(user);

    return ResponseEntity.ok(ApiResponse.success("User role changed to " + role,
        UserManagementResponse.fromEntity(user)));
  }


  @GetMapping("/stats/summary")
  @Operation(summary = "Get user statistics summary")
  public ResponseEntity<ApiResponse<UserStatsSummary>> getUserStatsSummary() {
    long totalUsers = userRepository.count();
    long adminCount = userRepository.countByRole(Role.ADMIN);
    long customerCount = userRepository.countByRole(Role.CUSTOMER);
    long enabledCount = userRepository.countByEnabledTrue();
    long disabledCount = totalUsers - enabledCount;
    long lockedCount = userRepository.countByLockedTrue();

    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
    long newUsersLast30Days = userRepository.countByCreatedAtAfter(thirtyDaysAgo);

    UserStatsSummary summary = UserStatsSummary.builder()
        .totalUsers(totalUsers)
        .adminCount(adminCount)
        .customerCount(customerCount)
        .enabledCount(enabledCount)
        .disabledCount(disabledCount)
        .lockedCount(lockedCount)
        .newUsersLast30Days(newUsersLast30Days)
        .build();

    return ResponseEntity.ok(ApiResponse.success("User statistics retrieved", summary));
  }


  private UserManagementResponse mapUserWithStats(User user) {
    String userIdStr = user.getId().toString();

    long orderCount = orderRepository.countByUserId(userIdStr);
    BigDecimal totalSpent = orderRepository.getTotalSpentByUser(userIdStr);
    if (totalSpent == null) totalSpent = BigDecimal.ZERO;

    long reviewCount = reviewRepository.countByUserId(user.getId());

    LocalDateTime lastOrderAt = orderRepository.findLastOrderDateByUserId(userIdStr);

    return UserManagementResponse.fromEntityWithStats(
        user, orderCount, totalSpent, reviewCount, lastOrderAt);
  }


  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class UserStatsSummary {
    private long totalUsers;
    private long adminCount;
    private long customerCount;
    private long enabledCount;
    private long disabledCount;
    private long lockedCount;
    private long newUsersLast30Days;
  }
}