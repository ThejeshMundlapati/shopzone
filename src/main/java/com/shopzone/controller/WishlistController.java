package com.shopzone.controller;

import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.response.CartResponse;
import com.shopzone.dto.response.WishlistResponse;
import com.shopzone.model.User;
import com.shopzone.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Wishlist management APIs")
//@SecurityRequirement(name = "Bearer Authentication")
public class WishlistController {

  private final WishlistService wishlistService;

  @GetMapping
  @Operation(summary = "Get wishlist", description = "Get current user's wishlist")
  public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist(
      @AuthenticationPrincipal User user) {
    WishlistResponse wishlist = wishlistService.getWishlist(user.getId().toString());
    return ResponseEntity.ok(ApiResponse.success("Wishlist retrieved successfully", wishlist));
  }

  @PostMapping("/add/{productId}")
  @Operation(summary = "Add to wishlist", description = "Add a product to the wishlist")
  public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
      @AuthenticationPrincipal User user,
      @PathVariable String productId) {
    WishlistResponse wishlist = wishlistService.addToWishlist(user.getId().toString(), productId);
    return ResponseEntity.ok(ApiResponse.success("Product added to wishlist", wishlist));
  }

  @DeleteMapping("/remove/{productId}")
  @Operation(summary = "Remove from wishlist", description = "Remove a product from the wishlist")
  public ResponseEntity<ApiResponse<WishlistResponse>> removeFromWishlist(
      @AuthenticationPrincipal User user,
      @PathVariable String productId) {
    WishlistResponse wishlist = wishlistService.removeFromWishlist(user.getId().toString(), productId);
    return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist", wishlist));
  }

  @PostMapping("/move-to-cart/{productId}")
  @Operation(summary = "Move to cart", description = "Move a product from wishlist to cart")
  public ResponseEntity<ApiResponse<CartResponse>> moveToCart(
      @AuthenticationPrincipal User user,
      @PathVariable String productId) {
    CartResponse cart = wishlistService.moveToCart(user.getId().toString(), productId);
    return ResponseEntity.ok(ApiResponse.success("Product moved to cart", cart));
  }

  @PostMapping("/move-all-to-cart")
  @Operation(summary = "Move all to cart", description = "Move all in-stock wishlist items to cart")
  public ResponseEntity<ApiResponse<CartResponse>> moveAllToCart(
      @AuthenticationPrincipal User user) {
    CartResponse cart = wishlistService.moveAllToCart(user.getId().toString());
    return ResponseEntity.ok(ApiResponse.success("All items moved to cart", cart));
  }

  @DeleteMapping("/clear")
  @Operation(summary = "Clear wishlist", description = "Remove all items from wishlist")
  public ResponseEntity<ApiResponse<Void>> clearWishlist(
      @AuthenticationPrincipal User user) {
    wishlistService.clearWishlist(user.getId().toString());
    return ResponseEntity.ok(ApiResponse.success("Wishlist cleared successfully", null));
  }

  @GetMapping("/check/{productId}")
  @Operation(summary = "Check if in wishlist", description = "Check if a product is in the wishlist")
  public ResponseEntity<ApiResponse<Boolean>> isInWishlist(
      @AuthenticationPrincipal User user,
      @PathVariable String productId) {
    boolean inWishlist = wishlistService.isInWishlist(user.getId().toString(), productId);
    return ResponseEntity.ok(ApiResponse.success(
        inWishlist ? "Product is in wishlist" : "Product is not in wishlist",
        inWishlist));
  }
}