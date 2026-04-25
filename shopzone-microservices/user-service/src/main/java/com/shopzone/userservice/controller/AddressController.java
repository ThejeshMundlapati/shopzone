package com.shopzone.userservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.userservice.dto.request.AddressRequest;
import com.shopzone.userservice.dto.response.AddressResponse;
import com.shopzone.userservice.model.User;
import com.shopzone.userservice.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Address management APIs")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Get all addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved",
                addressService.getUserAddresses(user.getId().toString())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getById(
            @AuthenticationPrincipal User user, @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Address retrieved",
                addressService.getAddressById(user.getId().toString(), id)));
    }

    @GetMapping("/default")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefault(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Default address",
                addressService.getDefaultAddress(user.getId().toString())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address created",
                        addressService.createAddress(user.getId().toString(), request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> update(
            @AuthenticationPrincipal User user, @PathVariable String id,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Address updated",
                addressService.updateAddress(user.getId().toString(), id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal User user, @PathVariable String id) {
        addressService.deleteAddress(user.getId().toString(), id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted"));
    }

    @PatchMapping("/{id}/set-default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(
            @AuthenticationPrincipal User user, @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Default set",
                addressService.setAsDefault(user.getId().toString(), id)));
    }
}
