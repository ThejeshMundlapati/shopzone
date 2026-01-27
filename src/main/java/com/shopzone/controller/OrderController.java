package com.shopzone.controller;

import com.shopzone.dto.request.CancelOrderRequest;
import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.response.OrderResponse;
import com.shopzone.dto.response.OrderSummaryResponse;
import com.shopzone.model.User;
import com.shopzone.model.enums.OrderStatus;
import com.shopzone.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "User order management APIs")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

  private final OrderService orderService;

  @GetMapping
  @Operation(summary = "Get my orders", description = "Get paginated list of current user's orders")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orders retrieved"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
  })
  public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getMyOrders(
      @AuthenticationPrincipal User user,
      @Parameter(description = "Filter by status") @RequestParam(required = false) OrderStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    String userId = user.getId().toString();
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    Page<OrderSummaryResponse> orders;
    if (status != null) {
      orders = orderService.getUserOrdersByStatus(userId, status, pageable);
    } else {
      orders = orderService.getUserOrders(userId, pageable);
    }

    return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
  }

  @GetMapping("/{orderNumber}")
  @Operation(summary = "Get order details", description = "Get detailed information about a specific order")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not your order"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
  })
  public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(
      @AuthenticationPrincipal User user,
      @PathVariable String orderNumber) {

    String userId = user.getId().toString();
    OrderResponse order = orderService.getOrderForUser(userId, orderNumber);
    return ResponseEntity.ok(ApiResponse.success("Order details retrieved", order));
  }

  @PostMapping("/{orderNumber}/cancel")
  @Operation(summary = "Cancel order", description = "Cancel an order (within cancellation window)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order cancelled"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot cancel order"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not your order"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
  })
  public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
      @AuthenticationPrincipal User user,
      @PathVariable String orderNumber,
      @Valid @RequestBody CancelOrderRequest request) {

    String userId = user.getId().toString();
    OrderResponse order = orderService.cancelOrder(userId, orderNumber, request);
    return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
  }

  @GetMapping("/{orderNumber}/track")
  @Operation(summary = "Track order", description = "Get order tracking information")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tracking info retrieved"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not your order"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
  })
  public ResponseEntity<ApiResponse<OrderTrackingResponse>> trackOrder(
      @AuthenticationPrincipal User user,
      @PathVariable String orderNumber) {

    String userId = user.getId().toString();
    OrderResponse order = orderService.getOrderForUser(userId, orderNumber);

    OrderTrackingResponse tracking = OrderTrackingResponse.builder()
        .orderNumber(order.getOrderNumber())
        .status(order.getStatus())
        .statusDisplayName(order.getStatusDisplayName())
        .trackingNumber(order.getTrackingNumber())
        .shippingCarrier(order.getShippingCarrier())
        .createdAt(order.getCreatedAt())
        .confirmedAt(order.getConfirmedAt())
        .shippedAt(order.getShippedAt())
        .deliveredAt(order.getDeliveredAt())
        .build();

    return ResponseEntity.ok(ApiResponse.success("Tracking info retrieved", tracking));
  }

  @GetMapping("/count")
  @Operation(summary = "Get my order count", description = "Get total number of orders for current user")
  public ResponseEntity<ApiResponse<Long>> getOrderCount(@AuthenticationPrincipal User user) {
    String userId = user.getId().toString();
    long count = orderService.getUserOrderCount(userId);
    return ResponseEntity.ok(ApiResponse.success("Order count retrieved", count));
  }

  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class OrderTrackingResponse {
    private String orderNumber;
    private OrderStatus status;
    private String statusDisplayName;
    private String trackingNumber;
    private String shippingCarrier;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime confirmedAt;
    private java.time.LocalDateTime shippedAt;
    private java.time.LocalDateTime deliveredAt;
  }
}