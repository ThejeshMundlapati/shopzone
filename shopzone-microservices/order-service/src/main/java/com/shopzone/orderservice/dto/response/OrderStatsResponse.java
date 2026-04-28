package com.shopzone.orderservice.dto.response;
import com.shopzone.orderservice.model.enums.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderStatsResponse {
    private long totalOrders, ordersToday, ordersThisWeek, ordersThisMonth;
    private long pendingOrders, processingOrders, shippedOrders, deliveredOrders;
    private Map<OrderStatus, Long> ordersByStatus;
    private BigDecimal totalRevenue, averageOrderValue, cancellationRate;
}
