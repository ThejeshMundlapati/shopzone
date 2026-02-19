package com.shopzone.service;

import com.shopzone.dto.response.*;
import com.shopzone.model.Order;
import com.shopzone.model.OrderItem;
import com.shopzone.model.Role;
import com.shopzone.model.User;
import com.shopzone.model.enums.OrderStatus;
import com.shopzone.model.enums.PaymentStatus;
import com.shopzone.repository.jpa.OrderRepository;
import com.shopzone.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating admin reports.
 * Provides revenue, sales, and user growth reports.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;


  @Transactional(readOnly = true)
  public RevenueReportResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
    log.info("Generating revenue report from {} to {}", startDate, endDate);

    if (startDate == null) startDate = LocalDate.now().minusDays(30);
    if (endDate == null) endDate = LocalDate.now();

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

    List<Order> orders = orderRepository.findByCreatedAtBetween(startDateTime, endDateTime);

    List<Order> paidOrders = orders.stream()
        .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID
            || o.getPaymentStatus() == PaymentStatus.PARTIALLY_REFUNDED)
        .toList();

    BigDecimal totalRevenue = paidOrders.stream()
        .map(Order::getTotalAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalTax = paidOrders.stream()
        .map(Order::getTaxAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalShipping = paidOrders.stream()
        .map(Order::getShippingCost)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalDiscount = paidOrders.stream()
        .map(Order::getDiscountAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal averageOrderValue = !paidOrders.isEmpty()
        ? totalRevenue.divide(BigDecimal.valueOf(paidOrders.size()), 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

    Map<LocalDate, BigDecimal> dailyRevenue = paidOrders.stream()
        .collect(Collectors.groupingBy(
            o -> o.getCreatedAt().toLocalDate(),
            TreeMap::new,
            Collectors.reducing(BigDecimal.ZERO, Order::getTotalAmount, BigDecimal::add)
        ));

    List<DailyRevenueEntry> dailyRevenueList = dailyRevenue.entrySet().stream()
        .map(e -> DailyRevenueEntry.builder()
            .date(e.getKey())
            .revenue(e.getValue())
            .orderCount(countOrdersOnDate(paidOrders, e.getKey()))
            .build())
        .collect(Collectors.toList());

    return RevenueReportResponse.builder()
        .startDate(startDate)
        .endDate(endDate)
        .totalRevenue(totalRevenue)
        .totalOrders((long) paidOrders.size())
        .totalTax(totalTax)
        .totalShipping(totalShipping)
        .totalDiscount(totalDiscount)
        .averageOrderValue(averageOrderValue)
        .dailyRevenue(dailyRevenueList)
        .build();
  }


  @Transactional(readOnly = true)
  public SalesReportResponse getSalesReport(LocalDate startDate, LocalDate endDate) {
    log.info("Generating sales report from {} to {}", startDate, endDate);

    if (startDate == null) startDate = LocalDate.now().minusDays(30);
    if (endDate == null) endDate = LocalDate.now();

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

    List<Order> orders = orderRepository.findByCreatedAtBetween(startDateTime, endDateTime);

    Map<String, Long> ordersByStatus = new LinkedHashMap<>();
    for (OrderStatus status : OrderStatus.values()) {
      long count = orders.stream().filter(o -> o.getStatus() == status).count();
      if (count > 0) {
        ordersByStatus.put(status.getDisplayName(), count);
      }
    }

    Map<String, Long> ordersByPaymentStatus = new LinkedHashMap<>();
    for (PaymentStatus status : PaymentStatus.values()) {
      long count = orders.stream().filter(o -> o.getPaymentStatus() == status).count();
      if (count > 0) {
        ordersByPaymentStatus.put(status.name(), count);
      }
    }

    Map<String, ProductSalesAggregator> productSales = new HashMap<>();
    for (Order order : orders) {
      if (order.getItems() == null) continue;
      for (OrderItem item : order.getItems()) {
        productSales.computeIfAbsent(item.getProductId(),
                k -> new ProductSalesAggregator(item.getProductId(), item.getProductName()))
            .addSale(item.getQuantity(), item.getTotalPrice());
      }
    }

    List<TopProductResponse> topProducts = productSales.values().stream()
        .sorted(Comparator.comparingInt(ProductSalesAggregator::getTotalQuantity).reversed())
        .limit(10)
        .map(agg -> TopProductResponse.builder()
            .productId(agg.getProductId())
            .productName(agg.getProductName())
            .totalQuantitySold(agg.getTotalQuantity())
            .totalRevenue(agg.getTotalRevenue())
            .orderCount(agg.getOrderCount())
            .build())
        .collect(Collectors.toList());

    long cancelledCount = orders.stream()
        .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
        .count();
    BigDecimal cancellationRate = !orders.isEmpty()
        ? BigDecimal.valueOf(cancelledCount)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

    return SalesReportResponse.builder()
        .startDate(startDate)
        .endDate(endDate)
        .totalOrders((long) orders.size())
        .ordersByStatus(ordersByStatus)
        .ordersByPaymentStatus(ordersByPaymentStatus)
        .topProducts(topProducts)
        .cancellationRate(cancellationRate)
        .cancelledOrders(cancelledCount)
        .build();
  }


  @Transactional(readOnly = true)
  public UserGrowthResponse getUserGrowthReport(LocalDate startDate, LocalDate endDate) {
    log.info("Generating user growth report from {} to {}", startDate, endDate);

    if (startDate == null) startDate = LocalDate.now().minusDays(30);
    if (endDate == null) endDate = LocalDate.now();

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

    long totalUsers = userRepository.count();

    List<User> newUsers = userRepository.findByCreatedAtBetween(startDateTime, endDateTime);
    long newUsersCount = newUsers.size();

    Map<String, Long> usersByRole = new LinkedHashMap<>();
    usersByRole.put("CUSTOMER", userRepository.countByRole(Role.CUSTOMER));
    usersByRole.put("ADMIN", userRepository.countByRole(Role.ADMIN));

    Map<LocalDate, Long> dailyRegistrations = newUsers.stream()
        .collect(Collectors.groupingBy(
            u -> u.getCreatedAt().toLocalDate(),
            TreeMap::new,
            Collectors.counting()
        ));

    List<DailyUserEntry> dailyUserList = dailyRegistrations.entrySet().stream()
        .map(e -> DailyUserEntry.builder()
            .date(e.getKey())
            .newUsers(e.getValue())
            .build())
        .collect(Collectors.toList());

    long verifiedUsers = userRepository.countByEmailVerifiedTrue();
    BigDecimal verificationRate = totalUsers > 0
        ? BigDecimal.valueOf(verifiedUsers)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

    return UserGrowthResponse.builder()
        .startDate(startDate)
        .endDate(endDate)
        .totalUsers(totalUsers)
        .newUsersInPeriod(newUsersCount)
        .usersByRole(usersByRole)
        .dailyRegistrations(dailyUserList)
        .verifiedUsers(verifiedUsers)
        .verificationRate(verificationRate)
        .build();
  }


  private int countOrdersOnDate(List<Order> orders, LocalDate date) {
    return (int) orders.stream()
        .filter(o -> o.getCreatedAt().toLocalDate().equals(date))
        .count();
  }


  @lombok.Data
  private static class ProductSalesAggregator {
    private String productId;
    private String productName;
    private int totalQuantity;
    private BigDecimal totalRevenue;
    private int orderCount;

    ProductSalesAggregator(String productId, String productName) {
      this.productId = productId;
      this.productName = productName;
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
}