package com.shopzone.cartservice.controller;

import com.shopzone.cartservice.dto.request.AddToCartRequest;
import com.shopzone.cartservice.dto.request.UpdateCartItemRequest;
import com.shopzone.cartservice.dto.response.CartResponse;
import com.shopzone.cartservice.service.CartService;
import com.shopzone.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/cart") @RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart APIs")
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", cartService.getCart(getUserId(auth))));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(Authentication auth, @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Added to cart", cartService.addToCart(getUserId(auth), request)));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartResponse>> update(Authentication auth, @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cartService.updateCartItem(getUserId(auth), request)));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> remove(Authentication auth, @PathVariable String productId) {
        return ResponseEntity.ok(ApiResponse.success("Removed", cartService.removeFromCart(getUserId(auth), productId)));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clear(Authentication auth) {
        cartService.clearCart(getUserId(auth)); return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }

    private String getUserId(Authentication auth) { return (String) auth.getPrincipal(); }
}
