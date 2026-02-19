package com.shopzone.controller;

import com.shopzone.dto.response.*;
import com.shopzone.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Dashboard API - Statistics and analytics.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Dashboard", description = "Dashboard statistics and analytics APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

  private final DashboardService dashboardService;


  @GetMapping("/stats")
  @Operation(summary = "Get dashboard statistics",
      description = "Get comprehensive dashboard stats including orders, revenue, users, and products")
  public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
    log.info("Admin requesting dashboard statistics");
    DashboardStatsResponse stats = dashboardService.getDashboardStats();
    return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", stats));
  }


  @GetMapping("/recent-orders")
  @Operation(summary = "Get recent orders",
      description = "Get the most recent orders for the dashboard overview")
  public ResponseEntity<ApiResponse<List<RecentOrderResponse>>> getRecentOrders(
      @Parameter(description = "Number of recent orders to fetch (default: 10, max: 50)")
      @RequestParam(defaultValue = "10") int limit) {

    if (limit > 50) limit = 50;
    if (limit < 1) limit = 1;

    log.info("Admin requesting {} recent orders", limit);
    List<RecentOrderResponse> recentOrders = dashboardService.getRecentOrders(limit);
    return ResponseEntity.ok(ApiResponse.success("Recent orders retrieved successfully", recentOrders));
  }


  @GetMapping("/top-products")
  @Operation(summary = "Get top selling products",
      description = "Get top selling products by quantity sold")
  public ResponseEntity<ApiResponse<List<TopProductResponse>>> getTopProducts(
      @Parameter(description = "Number of top products to fetch (default: 10, max: 50)")
      @RequestParam(defaultValue = "10") int limit) {

    if (limit > 50) limit = 50;
    if (limit < 1) limit = 1;

    log.info("Admin requesting top {} products", limit);
    List<TopProductResponse> topProducts = dashboardService.getTopSellingProducts(limit);
    return ResponseEntity.ok(ApiResponse.success("Top products retrieved successfully", topProducts));
  }


  @GetMapping("/top-customers")
  @Operation(summary = "Get top customers",
      description = "Get top customers by total spending")
  public ResponseEntity<ApiResponse<List<TopCustomerResponse>>> getTopCustomers(
      @Parameter(description = "Number of top customers to fetch (default: 10, max: 50)")
      @RequestParam(defaultValue = "10") int limit) {

    if (limit > 50) limit = 50;
    if (limit < 1) limit = 1;

    log.info("Admin requesting top {} customers", limit);
    List<TopCustomerResponse> topCustomers = dashboardService.getTopCustomers(limit);
    return ResponseEntity.ok(ApiResponse.success("Top customers retrieved successfully", topCustomers));
  }
}