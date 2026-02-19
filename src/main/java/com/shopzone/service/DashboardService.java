package com.shopzone.service;

import com.shopzone.dto.response.*;
import com.shopzone.model.Order;
import com.shopzone.model.OrderItem;
import com.shopzone.model.enums.OrderStatus;
import com.shopzone.model.enums.PaymentStatus;
import com.shopzone.repository.jpa.OrderRepository;
import com.shopzone.repository.jpa.ReviewRepository;
import com.shopzone.repository.jpa.UserRepository;
import com.shopzone.repository.mongo.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for admin dashboard statistics.
 * Aggregates data from orders, users, products, and reviews.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final ReviewRepository reviewRepository;


  @Transactional(readOnly = true)
  public DashboardStatsResponse getDashboardStats() {
    log.info("Generating dashboard statistics");

    LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
    LocalDateTime startOfWeek = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .atStartOfDay();
    LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

    long totalOrders = orderRepository.count();

    Map<String, Long> ordersByStatus = new LinkedHashMap<>();
    for (OrderStatus status : OrderStatus.values()) {
      ordersByStatus.put(status.name(), orderRepository.countByStatus(status));
    }

    long ordersToday = orderRepository.countByCreatedAtAfter(startOfToday);
    long ordersThisWeek = orderRepository.countByCreatedAtAfter(startOfWeek);
    long ordersThisMonth = orderRepository.countByCreatedAtAfter(startOfMonth);

    long pendingOrders = ordersByStatus.getOrDefault("PENDING", 0L)
        + ordersByStatus.getOrDefault("CONFIRMED", 0L);
    long processingOrders = ordersByStatus.getOrDefault("PROCESSING", 0L);
    long shippedOrders = ordersByStatus.getOrDefault("SHIPPED", 0L);
    long deliveredOrders = ordersByStatus.getOrDefault("DELIVERED", 0L);
    long cancelledOrders = ordersByStatus.getOrDefault("CANCELLED", 0L);

    BigDecimal totalRevenue = orderRepository.getTotalRevenue();
    if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

    BigDecimal revenueToday = orderRepository.calculateRevenueSince(startOfToday);
    if (revenueToday == null) revenueToday = BigDecimal.ZERO;

    BigDecimal revenueThisWeek = orderRepository.calculateRevenueSince(startOfWeek);
    if (revenueThisWeek == null) revenueThisWeek = BigDecimal.ZERO;

    BigDecimal revenueThisMonth = orderRepository.calculateRevenueSince(startOfMonth);
    if (revenueThisMonth == null) revenueThisMonth = BigDecimal.ZERO;

    BigDecimal averageOrderValue = totalOrders > 0
        ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

    long totalUsers = userRepository.count();
    long newUsersToday = userRepository.countByCreatedAtAfter(startOfToday);
    long newUsersThisWeek = userRepository.countByCreatedAtAfter(startOfWeek);
    long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);

    long totalProducts = productRepository.count();
    long activeProducts = productRepository.countByActiveTrue();
    long outOfStockProducts = productRepository.countOutOfStock();
    long lowStockProducts = productRepository.countLowStock(10);

    long totalReviews = reviewRepository.count();

    BigDecimal cancellationRate = totalOrders > 0
        ? BigDecimal.valueOf(cancelledOrders)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

    return DashboardStatsResponse.builder()
        .totalOrders(totalOrders)
        .ordersToday(ordersToday)
        .ordersThisWeek(ordersThisWeek)
        .ordersThisMonth(ordersThisMonth)
        .ordersByStatus(ordersByStatus)
        .pendingOrders(pendingOrders)
        .processingOrders(processingOrders)
        .shippedOrders(shippedOrders)
        .deliveredOrders(deliveredOrders)
        .cancelledOrders(cancelledOrders)
        .totalRevenue(totalRevenue)
        .revenueToday(revenueToday)
        .revenueThisWeek(revenueThisWeek)
        .revenueThisMonth(revenueThisMonth)
        .averageOrderValue(averageOrderValue)
        .totalUsers(totalUsers)
        .newUsersToday(newUsersToday)
        .newUsersThisWeek(newUsersThisWeek)
        .newUsersThisMonth(newUsersThisMonth)
        .totalProducts(totalProducts)
        .activeProducts(activeProducts)
        .outOfStockProducts(outOfStockProducts)
        .lowStockProducts(lowStockProducts)
        .totalReviews(totalReviews)
        .cancellationRate(cancellationRate)
        .build();
  }


  @Transactional(readOnly = true)
  public List<RecentOrderResponse> getRecentOrders(int limit) {
    log.debug("Fetching {} recent orders", limit);

    List<Order> recentOrders = orderRepository.findRecentOrders(PageRequest.of(0, limit));

    return recentOrders.stream()
        .map(order -> RecentOrderResponse.builder()
            .orderNumber(order.getOrderNumber())
            .customerName(order.getUserFullName())
            .customerEmail(order.getUserEmail())
            .status(order.getStatus().name())
            .statusDisplayName(order.getStatus().getDisplayName())
            .paymentStatus(order.getPaymentStatus().name())
            .totalAmount(order.getTotalAmount())
            .itemCount(order.getItems() != null ? order.getItems().size() : 0)
            .createdAt(order.getCreatedAt())
            .build())
        .collect(Collectors.toList());
  }


  @Transactional(readOnly = true)
  public List<TopProductResponse> getTopSellingProducts(int limit) {
    log.debug("Fetching top {} selling products", limit);

    List<Order> completedOrders = orderRepository.findByStatusIn(
        List.of(OrderStatus.DELIVERED, OrderStatus.SHIPPED,
            OrderStatus.PROCESSING, OrderStatus.CONFIRMED));

    Map<String, TopProductAggregator> productSales = new HashMap<>();

    for (Order order : completedOrders) {
      if (order.getItems() == null) continue;
      for (OrderItem item : order.getItems()) {
        productSales.computeIfAbsent(item.getProductId(),
                k -> new TopProductAggregator(item.getProductId(),
                    item.getProductName(), item.getProductImage()))
            .addSale(item.getQuantity(), item.getTotalPrice());
      }
    }

    return productSales.values().stream()
        .sorted(Comparator.comparingInt(TopProductAggregator::getTotalQuantity).reversed())
        .limit(limit)
        .map(agg -> TopProductResponse.builder()
            .productId(agg.getProductId())
            .productName(agg.getProductName())
            .productImage(agg.getProductImage())
            .totalQuantitySold(agg.getTotalQuantity())
            .totalRevenue(agg.getTotalRevenue())
            .orderCount(agg.getOrderCount())
            .build())
        .collect(Collectors.toList());
  }


  @Transactional(readOnly = true)
  public List<TopCustomerResponse> getTopCustomers(int limit) {
    log.debug("Fetching top {} customers", limit);

    List<Order> paidOrders = orderRepository.findByPaymentStatus(PaymentStatus.PAID);

    Map<String, TopCustomerAggregator> customerSpending = new HashMap<>();

    for (Order order : paidOrders) {
      customerSpending.computeIfAbsent(order.getUserId(),
              k -> new TopCustomerAggregator(order.getUserId(),
                  order.getUserFullName(), order.getUserEmail()))
          .addOrder(order.getTotalAmount());
    }

    return customerSpending.values().stream()
        .sorted(Comparator.comparing(TopCustomerAggregator::getTotalSpent).reversed())
        .limit(limit)
        .map(agg -> TopCustomerResponse.builder()
            .userId(agg.getUserId())
            .customerName(agg.getCustomerName())
            .customerEmail(agg.getCustomerEmail())
            .totalOrders(agg.getOrderCount())
            .totalSpent(agg.getTotalSpent())
            .averageOrderValue(agg.getOrderCount() > 0
                ? agg.getTotalSpent().divide(
                BigDecimal.valueOf(agg.getOrderCount()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO)
            .build())
        .collect(Collectors.toList());
  }


  @lombok.Data
  @lombok.AllArgsConstructor
  private static class TopProductAggregator {
    private String productId;
    private String productName;
    private String productImage;
    private int totalQuantity;
    private BigDecimal totalRevenue;
    private int orderCount;

    TopProductAggregator(String productId, String productName, String productImage) {
      this.productId = productId;
      this.productName = productName;
      this.productImage = productImage;
      this.totalQuantity = 0;
      this.totalRevenue = BigDecimal.ZERO;
      this.orderCount = 0;
    }

    void addSale(int quantity, BigDecimal revenue) {
      this.totalQuantity += quantity;
      this.totalRevenue = this.totalRevenue.add(revenue != null ? revenue : BigDecimal.ZERO);
      this.orderCount++;
    }
  }

  @lombok.Data
  @lombok.AllArgsConstructor
  private static class TopCustomerAggregator {
    private String userId;
    private String customerName;
    private String customerEmail;
    private int orderCount;
    private BigDecimal totalSpent;

    TopCustomerAggregator(String userId, String customerName, String customerEmail) {
      this.userId = userId;
      this.customerName = customerName;
      this.customerEmail = customerEmail;
      this.orderCount = 0;
      this.totalSpent = BigDecimal.ZERO;
    }

    void addOrder(BigDecimal amount) {
      this.orderCount++;
      this.totalSpent = this.totalSpent.add(amount != null ? amount : BigDecimal.ZERO);
    }
  }
}