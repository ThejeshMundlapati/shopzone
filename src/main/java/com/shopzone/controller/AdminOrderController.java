package com.shopzone.controller;

import com.shopzone.dto.request.UpdateOrderStatusRequest;
import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.response.OrderResponse;
import com.shopzone.dto.response.OrderStatsResponse;
import com.shopzone.dto.response.OrderSummaryResponse;
import com.shopzone.model.enums.OrderStatus;
import com.shopzone.model.enums.PaymentStatus;
import com.shopzone.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Orders", description = "Order management for administrators")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

  private final OrderService orderService;

  @GetMapping
  @Operation(summary = "Get all orders",
      description = "Get all orders with optional filters (admin only)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orders retrieved"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized - Admin only")
  })
  public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getAllOrders(
      @Parameter(description = "Filter by order status")
      @RequestParam(required = false) OrderStatus status,

      @Parameter(description = "Filter by payment status")
      @RequestParam(required = false) PaymentStatus paymentStatus,

      @Parameter(description = "Filter orders after this date")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

      @Parameter(description = "Filter orders before this date")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    log.info("Admin fetching orders - status: {}, paymentStatus: {}", status, paymentStatus);

    Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<OrderSummaryResponse> orders = orderService.getAllOrders(
        status, paymentStatus, startDate, endDate, pageable);

    return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orders));
  }

  @GetMapping("/{orderNumber}")
  @Operation(summary = "Get order details (admin)",
      description = "Get detailed information about any order")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
  })
  public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(
      @PathVariable String orderNumber) {

    log.info("Admin fetching order: {}", orderNumber);

    OrderResponse order = orderService.getOrderByNumberResponse(orderNumber);

    return ResponseEntity.ok(ApiResponse.success("Order found", order));
  }

  @PatchMapping("/{orderNumber}/status")
  @Operation(summary = "Update order status",
      description = "Update order status with optional tracking info (for shipping)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status transition"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
  })
  public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
      @PathVariable String orderNumber,
      @Valid @RequestBody UpdateOrderStatusRequest request) {

    log.info("Admin updating order {} status to: {}", orderNumber, request.getStatus());
    OrderResponse order = orderService.updateOrderStatus(orderNumber, request);

    return ResponseEntity.ok(ApiResponse.success(
        "Order status updated to " + request.getStatus().getDisplayName(), order));
  }

  @GetMapping("/search")
  @Operation(summary = "Search orders",
      description = "Search orders by order number, customer email, or customer name")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized")
  })
  public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> searchOrders(
      @Parameter(description = "Search query (order number, email, or name)")
      @RequestParam String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    log.info("Admin searching orders with query: {}", query);
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<OrderSummaryResponse> orders = orderService.searchOrders(query, pageable);

    return ResponseEntity.ok(ApiResponse.success("Search results", orders));
  }

  @GetMapping("/stats")
  @Operation(summary = "Get order statistics",
      description = "Get order statistics for dashboard (counts, revenue, etc.)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized")
  })
  public ResponseEntity<ApiResponse<OrderStatsResponse>> getOrderStats() {
    log.info("Admin fetching order statistics");
    OrderStatsResponse stats = orderService.getOrderStats();
    return ResponseEntity.ok(ApiResponse.success("Order statistics", stats));
  }
}