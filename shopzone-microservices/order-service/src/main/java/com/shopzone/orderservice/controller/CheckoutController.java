package com.shopzone.orderservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.orderservice.dto.request.CheckoutRequest;
import com.shopzone.orderservice.dto.response.OrderWithPaymentResponse;
import com.shopzone.orderservice.service.CheckoutService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/checkout") @RequiredArgsConstructor
@Tag(name = "Checkout", description = "Checkout APIs")
public class CheckoutController {
    private final CheckoutService checkoutService;

    @PostMapping("/place-order")
    public ResponseEntity<ApiResponse<OrderWithPaymentResponse>> placeOrder(
            Authentication auth, @Valid @RequestBody CheckoutRequest request) {
        String userId = (String) auth.getPrincipal();
        OrderWithPaymentResponse response = checkoutService.placeOrderWithPayment(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Order placed: " + response.getOrder().getOrderNumber(), response));
    }
}
