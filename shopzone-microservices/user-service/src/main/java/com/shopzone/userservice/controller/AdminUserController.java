package com.shopzone.userservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.common.exception.ResourceNotFoundException;
import com.shopzone.userservice.dto.request.AdminUserUpdateRequest;
import com.shopzone.userservice.dto.response.UserManagementResponse;
import com.shopzone.userservice.model.Role;
import com.shopzone.userservice.model.User;
import com.shopzone.userservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
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

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Users", description = "Admin user management")
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<Page<UserManagementResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Role role, @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> users;
        if (role != null && search != null) users = userRepository.findByRoleAndSearch(role, search, pageable);
        else if (role != null) users = userRepository.findByRole(role, pageable);
        else if (search != null) users = userRepository.searchUsers(search, pageable);
        else users = userRepository.findAll(pageable);

        return ResponseEntity.ok(ApiResponse.success("Users retrieved",
                users.map(UserManagementResponse::fromEntity)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserManagementResponse>> getUser(@PathVariable String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success("User found", UserManagementResponse.fromEntity(user)));
    }

    @PatchMapping("/{userId}/enable")
    public ResponseEntity<ApiResponse<UserManagementResponse>> enable(@PathVariable String userId) {
        User user = findUser(userId);
        user.setEnabled(true); user.setLocked(false);
        return ResponseEntity.ok(ApiResponse.success("User enabled",
                UserManagementResponse.fromEntity(userRepository.save(user))));
    }

    @PatchMapping("/{userId}/disable")
    public ResponseEntity<ApiResponse<UserManagementResponse>> disable(@PathVariable String userId) {
        User user = findUser(userId);
        user.setEnabled(false);
        return ResponseEntity.ok(ApiResponse.success("User disabled",
                UserManagementResponse.fromEntity(userRepository.save(user))));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UserManagementResponse>> changeRole(
            @PathVariable String userId, @RequestParam Role role) {
        User user = findUser(userId);
        user.setRole(role);
        return ResponseEntity.ok(ApiResponse.success("Role changed",
                UserManagementResponse.fromEntity(userRepository.save(user))));
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<ApiResponse<UserStatsSummary>> stats() {
        long total = userRepository.count();
        return ResponseEntity.ok(ApiResponse.success("Stats", UserStatsSummary.builder()
                .totalUsers(total)
                .adminCount(userRepository.countByRole(Role.ADMIN))
                .customerCount(userRepository.countByRole(Role.CUSTOMER))
                .enabledCount(userRepository.countByEnabledTrue())
                .disabledCount(total - userRepository.countByEnabledTrue())
                .lockedCount(userRepository.countByLockedTrue())
                .newUsersLast30Days(userRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(30)))
                .build()));
    }

    private User findUser(String id) {
        return userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class UserStatsSummary {
        private long totalUsers, adminCount, customerCount, enabledCount, disabledCount, lockedCount, newUsersLast30Days;
    }
}
