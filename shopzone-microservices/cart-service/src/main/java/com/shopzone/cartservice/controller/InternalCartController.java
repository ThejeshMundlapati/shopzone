package com.shopzone.cartservice.controller;

import com.shopzone.cartservice.dto.response.CartResponse;
import com.shopzone.cartservice.service.CartService;
import com.shopzone.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/internal/cart") @RequiredArgsConstructor @Hidden
public class InternalCartController {
    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success("Cart", cartService.getCart(userId)));
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }
}
