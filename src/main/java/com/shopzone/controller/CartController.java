package com.shopzone.controller;

import com.shopzone.dto.request.AddToCartRequest;
import com.shopzone.dto.request.UpdateCartItemRequest;
import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.response.CartResponse;
import com.shopzone.model.User;
import com.shopzone.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management APIs")
public class CartController {

  private final CartService cartService;

  @GetMapping
  @Operation(summary = "Get cart", description = "Get current user's shopping cart")
  public ResponseEntity<ApiResponse<CartResponse>> getCart(
      @AuthenticationPrincipal User user) {
    CartResponse cart = cartService.getCart(user.getId().toString());
    return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
  }

  @PostMapping("/add")
  @Operation(summary = "Add to cart", description = "Add a product to the shopping cart")
  public ResponseEntity<ApiResponse<CartResponse>> addToCart(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody AddToCartRequest request) {
    CartResponse cart = cartService.addToCart(user.getId().toString(), request);
    return ResponseEntity.ok(ApiResponse.success("Product added to cart", cart));
  }

  @PutMapping("/update")
  @Operation(summary = "Update cart item", description = "Update quantity of an item in the cart")
  public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UpdateCartItemRequest request) {
    CartResponse cart = cartService.updateCartItem(user.getId().toString(), request);
    return ResponseEntity.ok(ApiResponse.success("Cart updated successfully", cart));
  }

  @DeleteMapping("/remove/{productId}")
  @Operation(summary = "Remove from cart", description = "Remove a product from the cart")
  public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
      @AuthenticationPrincipal User user,
      @PathVariable String productId) {
    CartResponse cart = cartService.removeFromCart(user.getId().toString(), productId);
    return ResponseEntity.ok(ApiResponse.success("Product removed from cart", cart));
  }

  @DeleteMapping("/clear")
  @Operation(summary = "Clear cart", description = "Remove all items from the cart")
  public ResponseEntity<ApiResponse<Void>> clearCart(
      @AuthenticationPrincipal User user) {
    cartService.clearCart(user.getId().toString());
    return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
  }

  @GetMapping("/validate")
  @Operation(summary = "Validate cart", description = "Validate cart for checkout (check stock, prices)")
  public ResponseEntity<ApiResponse<CartResponse>> validateCart(
      @AuthenticationPrincipal User user) {
    CartResponse cart = cartService.validateCartForCheckout(user.getId().toString());
    return ResponseEntity.ok(ApiResponse.success("Cart is valid for checkout", cart));
  }
}