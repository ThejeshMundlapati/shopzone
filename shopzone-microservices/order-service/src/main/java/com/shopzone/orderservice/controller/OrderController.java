package com.shopzone.orderservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.common.dto.response.UserResponse;
import com.shopzone.orderservice.client.UserClient;
import com.shopzone.orderservice.dto.request.CancelOrderRequest;
import com.shopzone.orderservice.dto.response.*;
import com.shopzone.orderservice.model.enums.OrderStatus;
import com.shopzone.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/orders") @RequiredArgsConstructor
@Tag(name = "Orders", description = "User order management")
public class OrderController {
    private final OrderService orderService;
    private final UserClient userClient;

    private String resolveUserId(Authentication auth) {
        String email = (String) auth.getPrincipal();
        UserResponse user = userClient.getUserByEmail(email);
        return user.getId();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getMyOrders(
            Authentication auth, @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved",
            orderService.getUserOrders(resolveUserId(auth), status, pageable)));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(Authentication auth, @PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.success("Order details",
            orderService.getOrderForUser(resolveUserId(auth), orderNumber)));
    }

    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(
            Authentication auth, @PathVariable String orderNumber, @Valid @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled",
            orderService.cancelOrder(resolveUserId(auth), orderNumber, request)));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> count(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Count",
            orderService.countByUserId(resolveUserId(auth))));
    }
}
