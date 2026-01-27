package com.shopzone.dto.response;

import com.shopzone.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatsResponse {

  private long totalOrders;
  private Map<OrderStatus, Long> ordersByStatus;
  private BigDecimal totalRevenue;
  private BigDecimal averageOrderValue;
  private BigDecimal cancellationRate;

  private long ordersToday;
  private long ordersThisWeek;
  private long ordersThisMonth;

  private long pendingOrders;
  private long processingOrders;
  private long shippedOrders;
  private long deliveredOrders;
}