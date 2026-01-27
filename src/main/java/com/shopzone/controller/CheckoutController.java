package com.shopzone.controller;

import com.shopzone.dto.request.CheckoutRequest;
import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.response.CheckoutPreviewResponse;
import com.shopzone.dto.response.CheckoutValidationResponse;
import com.shopzone.dto.response.OrderResponse;
import com.shopzone.model.User;
import com.shopzone.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Checkout and order placement APIs")
@SecurityRequirement(name = "bearerAuth")
public class CheckoutController {

  private final CheckoutService checkoutService;

  @GetMapping("/validate")
  @Operation(summary = "Validate cart for checkout",
      description = "Checks product availability, stock levels, and price changes")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation complete"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
  })
  public ResponseEntity<ApiResponse<CheckoutValidationResponse>> validateCart(
      @AuthenticationPrincipal User user) {

    String userId = user.getId().toString();
    CheckoutValidationResponse validation = checkoutService.validateCart(userId);

    String message = validation.isValid()
        ? "Cart is ready for checkout"
        : "Cart has issues that need to be resolved";

    return ResponseEntity.ok(ApiResponse.success(message, validation));
  }

  @GetMapping("/preview")
  @Operation(summary = "Preview checkout totals",
      description = "Calculate and preview order totals including tax and shipping")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Preview calculated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cart has validation errors"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
  })
  public ResponseEntity<ApiResponse<CheckoutPreviewResponse>> previewCheckout(
      @AuthenticationPrincipal User user,
      @RequestParam String addressId) {

    String userId = user.getId().toString();
    CheckoutPreviewResponse preview = checkoutService.calculateTotals(userId, addressId);
    return ResponseEntity.ok(ApiResponse.success("Checkout preview calculated", preview));
  }

  @PostMapping("/place-order")
  @Operation(summary = "Place order",
      description = "Create order from cart, reserve stock, and clear cart")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order placed successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cart has validation errors"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
  })
  public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody CheckoutRequest request) {

    String userId = user.getId().toString();
    OrderResponse order = checkoutService.placeOrder(userId, request);
    return ResponseEntity.ok(ApiResponse.success(
        "Order placed successfully! Order number: " + order.getOrderNumber(),
        order));
  }
}