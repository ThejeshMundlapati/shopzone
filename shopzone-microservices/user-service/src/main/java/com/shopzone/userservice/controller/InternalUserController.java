package com.shopzone.userservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.common.dto.response.UserResponse;
import com.shopzone.userservice.dto.response.AddressResponse;
import com.shopzone.userservice.service.AddressService;
import com.shopzone.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal endpoints for inter-service communication.
 * NOT exposed through API Gateway — only reachable service-to-service.
 * No authentication required (trusted internal network).
 */
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
@Hidden
public class InternalUserController {

    private final UserService userService;
    private final AddressService addressService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success("User found", userService.getUserById(userId)));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.success("User found", userService.getUserByEmail(email)));
    }

    @GetMapping("/{userId}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddress(
            @PathVariable String userId, @PathVariable String addressId) {
        return ResponseEntity.ok(ApiResponse.success("Address found",
                addressService.getAddressById(userId, addressId)));
    }
}
