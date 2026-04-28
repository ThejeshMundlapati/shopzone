package com.shopzone.orderservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.orderservice.dto.response.OrderResponse;
import com.shopzone.orderservice.model.Order;
import com.shopzone.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController @RequestMapping("/api/internal/orders") @RequiredArgsConstructor @Hidden
public class InternalOrderController {
    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order", OrderResponse.fromEntity(orderService.findById(orderId))));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.success("Order", OrderResponse.fromEntity(orderService.findByOrderNumber(orderNumber))));
    }

    @PostMapping("/{orderId}/record-payment")
    public ResponseEntity<ApiResponse<Void>> recordPayment(@PathVariable String orderId, @RequestBody Map<String, String> body) {
        Order order = orderService.findById(orderId);
        order.recordPayment(body.get("chargeId"), body.get("receiptUrl"));
        orderService.save(order);
        orderService.reduceStockForOrder(order);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded"));
    }

    @PostMapping("/{orderId}/record-payment-failure")
    public ResponseEntity<ApiResponse<Void>> recordFailure(@PathVariable String orderId) {
        Order order = orderService.findById(orderId);
        order.recordPaymentFailure();
        orderService.save(order);
        return ResponseEntity.ok(ApiResponse.success("Failure recorded"));
    }

    @PostMapping("/{orderId}/record-refund")
    public ResponseEntity<ApiResponse<Void>> recordRefund(@PathVariable String orderId, @RequestBody Map<String, Object> body) {
        Order order = orderService.findById(orderId);
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        order.recordRefund(amount);
        orderService.save(order);
        if (Boolean.TRUE.equals(body.get("restoreStock"))) orderService.restoreStockForOrder(order);
        return ResponseEntity.ok(ApiResponse.success("Refund recorded"));
    }

    @PostMapping("/{orderId}/update-payment-intent")
    public ResponseEntity<ApiResponse<Void>> updatePaymentIntent(@PathVariable String orderId, @RequestBody Map<String, String> body) {
        Order order = orderService.findById(orderId);
        order.setStripePaymentIntentId(body.get("paymentIntentId"));
        order.setPaymentStatus(com.shopzone.orderservice.model.enums.PaymentStatus.AWAITING_PAYMENT);
        orderService.save(order);
        return ResponseEntity.ok(ApiResponse.success("Updated"));
    }
}
