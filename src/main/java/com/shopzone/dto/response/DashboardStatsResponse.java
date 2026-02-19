package com.shopzone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Comprehensive dashboard statistics response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponse {

  private long totalOrders;
  private long ordersToday;
  private long ordersThisWeek;
  private long ordersThisMonth;
  private Map<String, Long> ordersByStatus;
  private long pendingOrders;
  private long processingOrders;
  private long shippedOrders;
  private long deliveredOrders;
  private long cancelledOrders;

  private BigDecimal totalRevenue;
  private BigDecimal revenueToday;
  private BigDecimal revenueThisWeek;
  private BigDecimal revenueThisMonth;
  private BigDecimal averageOrderValue;

  private long totalUsers;
  private long newUsersToday;
  private long newUsersThisWeek;
  private long newUsersThisMonth;

  private long totalProducts;
  private long activeProducts;
  private long outOfStockProducts;
  private long lowStockProducts;

  private long totalReviews;

  private BigDecimal cancellationRate;
}