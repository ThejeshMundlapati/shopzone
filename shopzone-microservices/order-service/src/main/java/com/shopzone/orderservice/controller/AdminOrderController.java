package com.shopzone.orderservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.orderservice.dto.request.UpdateOrderStatusRequest;
import com.shopzone.orderservice.dto.response.*;
import com.shopzone.orderservice.model.enums.*;
import com.shopzone.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController @RequestMapping("/api/admin/orders") @RequiredArgsConstructor
@Tag(name = "Admin Orders", description = "Admin order management")
public class AdminOrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getAll(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(ApiResponse.success("Orders",
            orderService.getAllOrders(status, paymentStatus, startDate, endDate, PageRequest.of(page, size, sort))));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.success("Order", OrderResponse.fromEntity(orderService.findByOrderNumber(orderNumber))));
    }

    @PatchMapping("/{orderNumber}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable String orderNumber, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", orderService.updateOrderStatus(orderNumber, request)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> search(
            @RequestParam String query, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Results",
            orderService.searchOrders(query, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<OrderStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.success("Stats", orderService.getOrderStats()));
    }
}
