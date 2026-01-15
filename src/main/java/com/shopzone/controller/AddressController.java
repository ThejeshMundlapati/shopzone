package com.shopzone.controller;

import com.shopzone.dto.request.AddressRequest;
import com.shopzone.dto.response.AddressResponse;
import com.shopzone.dto.response.ApiResponse;
import com.shopzone.model.User;
import com.shopzone.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Address", description = "User address management APIs")
//@SecurityRequirement(name = "Bearer Authentication")
public class AddressController {

  private final AddressService addressService;

  @GetMapping
  @Operation(summary = "Get all addresses", description = "Get all addresses for current user")
  public ResponseEntity<ApiResponse<List<AddressResponse>>> getAllAddresses(
      @AuthenticationPrincipal User user) {
    List<AddressResponse> addresses = addressService.getUserAddresses(user.getId().toString());
    return ResponseEntity.ok(ApiResponse.success("Addresses retrieved successfully", addresses));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get address by ID", description = "Get a specific address by ID")
  public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
      @AuthenticationPrincipal User user,
      @PathVariable String id) {
    AddressResponse address = addressService.getAddressById(user.getId().toString(), id);
    return ResponseEntity.ok(ApiResponse.success("Address retrieved successfully", address));
  }

  @GetMapping("/default")
  @Operation(summary = "Get default address", description = "Get user's default address")
  public ResponseEntity<ApiResponse<AddressResponse>> getDefaultAddress(
      @AuthenticationPrincipal User user) {
    AddressResponse address = addressService.getDefaultAddress(user.getId().toString());
    return ResponseEntity.ok(ApiResponse.success("Default address retrieved successfully", address));
  }

  @PostMapping
  @Operation(summary = "Create address", description = "Create a new address")
  public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody AddressRequest request) {
    AddressResponse address = addressService.createAddress(user.getId().toString(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Address created successfully", address));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update address", description = "Update an existing address")
  public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
      @AuthenticationPrincipal User user,
      @PathVariable String id,
      @Valid @RequestBody AddressRequest request) {
    AddressResponse address = addressService.updateAddress(user.getId().toString(), id, request);
    return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete address", description = "Delete an address")
  public ResponseEntity<ApiResponse<Void>> deleteAddress(
      @AuthenticationPrincipal User user,
      @PathVariable String id) {
    addressService.deleteAddress(user.getId().toString(), id);
    return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
  }

  @PatchMapping("/{id}/set-default")
  @Operation(summary = "Set as default", description = "Set an address as the default address")
  public ResponseEntity<ApiResponse<AddressResponse>> setAsDefault(
      @AuthenticationPrincipal User user,
      @PathVariable String id) {
    AddressResponse address = addressService.setAsDefault(user.getId().toString(), id);
    return ResponseEntity.ok(ApiResponse.success("Address set as default", address));
  }
}