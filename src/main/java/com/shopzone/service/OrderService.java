package com.shopzone.service;

import com.shopzone.config.OrderConfig;
import com.shopzone.dto.request.CancelOrderRequest;
import com.shopzone.dto.request.UpdateOrderStatusRequest;
import com.shopzone.dto.response.OrderResponse;
import com.shopzone.dto.response.OrderStatsResponse;
import com.shopzone.dto.response.OrderSummaryResponse;
import com.shopzone.exception.BadRequestException;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.exception.UnauthorizedException;
import com.shopzone.model.Order;
import com.shopzone.model.OrderItem;
import com.shopzone.model.enums.OrderStatus;
import com.shopzone.model.enums.PaymentStatus;
import com.shopzone.repository.jpa.OrderRepository;
import com.shopzone.repository.mongo.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final OrderConfig orderConfig;


  /**
   * User Order Operations
   */

  @Transactional(readOnly = true)
  public Page<OrderSummaryResponse> getUserOrders(String userId, Pageable pageable) {
    log.debug("Fetching orders for user: {}", userId);
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
        .map(OrderSummaryResponse::fromEntity);
  }

  @Transactional(readOnly = true)
  public Page<OrderSummaryResponse> getUserOrdersByStatus(String userId, OrderStatus status, Pageable pageable) {
    return orderRepository.findByUserIdAndStatus(userId, status, pageable)
        .map(OrderSummaryResponse::fromEntity);
  }

  @Transactional(readOnly = true)
  public OrderResponse getOrderForUser(String userId, String orderNumber) {
    Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));

    if (!order.getUserId().equals(userId)) {
      throw new UnauthorizedException("You don't have permission to view this order");
    }

    return OrderResponse.fromEntity(order);
  }

  @Transactional
  public OrderResponse cancelOrder(String userId, String orderNumber, CancelOrderRequest request) {
    log.info("User {} requesting cancellation of order {}", userId, orderNumber);

    Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));

    if (!order.getUserId().equals(userId)) {
      throw new UnauthorizedException("You don't have permission to cancel this order");
    }

    if (!order.canCancel()) {
      throw new BadRequestException(
          "Order cannot be cancelled. Current status: " + order.getStatus().getDisplayName());
    }

    LocalDateTime cancellationDeadline = order.getCreatedAt()
        .plusHours(orderConfig.getCancellationWindowHours());
    if (LocalDateTime.now().isAfter(cancellationDeadline)) {
      throw new BadRequestException(
          "Cancellation window has expired. Orders can only be cancelled within " +
              orderConfig.getCancellationWindowHours() + " hours of placement.");
    }

    if (order.getPaymentStatus() == PaymentStatus.PAID) {
      throw new BadRequestException(
          "Paid orders cannot be cancelled directly. Please request a refund instead.");
    }

    order.setStatus(OrderStatus.CANCELLED);
    order.setCancelledAt(LocalDateTime.now());
    order.setCancellationReason(request.getReason());
    order.setCancelledBy("USER");
    order.setPaymentStatus(PaymentStatus.CANCELLED);

    restoreStock(order);

    order = orderRepository.save(order);
    log.info("Order {} cancelled by user {}", orderNumber, userId);

    return OrderResponse.fromEntity(order);
  }

  @Transactional(readOnly = true)
  public long getUserOrderCount(String userId) {
    return orderRepository.countByUserId(userId);
  }


  /**
   *  Admin Order Operations
   */

  @Transactional(readOnly = true)
  public Page<OrderSummaryResponse> getAllOrders(OrderStatus status,
                                                 PaymentStatus paymentStatus,
                                                 LocalDateTime startDate,
                                                 LocalDateTime endDate,
                                                 Pageable pageable) {
    return orderRepository.findWithFilters(status, paymentStatus, startDate, endDate, pageable)
        .map(OrderSummaryResponse::fromEntity);
  }

  @Transactional(readOnly = true)
  public OrderResponse getOrderByNumberResponse(String orderNumber) {
    Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
    return OrderResponse.fromEntity(order);
  }



  @Transactional
  public OrderResponse updateOrderStatus(String orderNumber, UpdateOrderStatusRequest request) {
    log.info("Admin updating order {} to status {}", orderNumber, request.getStatus());

    Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));

    OrderStatus newStatus = request.getStatus();

    if (!order.getStatus().canTransitionTo(newStatus)) {
      throw new BadRequestException(
          "Invalid status transition from " + order.getStatus() + " to " + newStatus);
    }

    if (newStatus == OrderStatus.SHIPPED) {
      if (request.getTrackingNumber() == null || request.getTrackingNumber().isBlank()) {
        throw new BadRequestException("Tracking number is required when marking order as shipped");
      }
      order.setTrackingNumber(request.getTrackingNumber());
      order.setShippingCarrier(request.getShippingCarrier());
    }

    if (newStatus == OrderStatus.CANCELLED) {
      order.setCancelledBy("ADMIN");
      restoreStock(order);
    }

    order.updateStatus(newStatus);

    if (request.getAdminNotes() != null && !request.getAdminNotes().isBlank()) {
      String existingNotes = order.getAdminNotes() != null ? order.getAdminNotes() + "\n" : "";
      order.setAdminNotes(existingNotes + "[" + LocalDateTime.now() + "] " + request.getAdminNotes());
    }

    order = orderRepository.save(order);
    log.info("Order {} status updated to {}", orderNumber, newStatus);

    return OrderResponse.fromEntity(order);
  }

  @Transactional(readOnly = true)
  public Page<OrderSummaryResponse> searchOrders(String query, Pageable pageable) {
    return orderRepository.searchOrders(query, pageable)
        .map(OrderSummaryResponse::fromEntity);
  }

  @Transactional(readOnly = true)
  public OrderStatsResponse getOrderStats() {
    LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
    LocalDateTime startOfWeek = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        .atStartOfDay();
    LocalDateTime startOfMonth = LocalDate.now()
        .withDayOfMonth(1)
        .atStartOfDay();

    Map<OrderStatus, Long> ordersByStatus = new HashMap<>();
    List<Object[]> statusCounts = orderRepository.getOrderCountsByStatus();
    for (Object[] row : statusCounts) {
      ordersByStatus.put((OrderStatus) row[0], (Long) row[1]);
    }

    long pending = ordersByStatus.getOrDefault(OrderStatus.PENDING, 0L);
    long confirmed = ordersByStatus.getOrDefault(OrderStatus.CONFIRMED, 0L);
    long processing = ordersByStatus.getOrDefault(OrderStatus.PROCESSING, 0L);
    long shipped = ordersByStatus.getOrDefault(OrderStatus.SHIPPED, 0L);
    long delivered = ordersByStatus.getOrDefault(OrderStatus.DELIVERED, 0L);
    long cancelled = ordersByStatus.getOrDefault(OrderStatus.CANCELLED, 0L);

    long totalOrders = orderRepository.count();
    BigDecimal totalRevenue = orderRepository.getTotalRevenue();
    if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

    BigDecimal avgOrderValue = orderRepository.getAverageOrderValue();
    if (avgOrderValue == null) avgOrderValue = BigDecimal.ZERO;

    BigDecimal cancellationRate = totalOrders > 0
        ? BigDecimal.valueOf(cancelled)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

    long ordersToday = orderRepository.countByCreatedAtAfter(startOfToday);
    long ordersThisWeek = orderRepository.countByCreatedAtAfter(startOfWeek);
    long ordersThisMonth = orderRepository.countByCreatedAtAfter(startOfMonth);

    return OrderStatsResponse.builder()
        .totalOrders(totalOrders)
        .ordersByStatus(ordersByStatus)
        .totalRevenue(totalRevenue)
        .averageOrderValue(avgOrderValue)
        .cancellationRate(cancellationRate)
        .ordersToday(ordersToday)
        .ordersThisWeek(ordersThisWeek)
        .ordersThisMonth(ordersThisMonth)
        .pendingOrders(pending)
        .processingOrders(processing)
        .shippedOrders(shipped)
        .deliveredOrders(delivered)
        .build();
  }


  /**
   * Get order entity by ID (internal use for PaymentService).
   */
  @Transactional(readOnly = true)
  public Order getOrderById(String orderId) {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
  }

  /**
   * Get order entity by order number (internal use for PaymentService).
   */
  @Transactional(readOnly = true)
  public Order getOrderByNumber(String orderNumber) {
    return orderRepository.findByOrderNumberWithItems(orderNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
  }

  /**
   * Save order (for PaymentService to update payment fields).
   */
  @Transactional
  public Order saveOrder(Order order) {
    return orderRepository.save(order);
  }

  /**
   * Reduce stock for all items in an order.
   * Called by PaymentService when payment succeeds via webhook.
   */
  @Transactional
  public void reduceStockForOrder(Order order) {
    log.info("Reducing stock for order: {}", order.getOrderNumber());

    for (OrderItem item : order.getItems()) {
      int result = productRepository.reduceStock(
          item.getProductId(),
          item.getQuantity(),
          -item.getQuantity()
      );

      if (result == 0) {
        log.error("Failed to reduce stock for product: {} in order: {}",
            item.getProductId(), order.getOrderNumber());
      } else {
        log.debug("Reduced stock for {} by {}", item.getProductId(), item.getQuantity());
      }
    }
  }

  /**
   * Restore stock for all items in an order (public method for RefundService).
   */
  @Transactional
  public void restoreStockForOrder(Order order) {
    log.info("Restoring stock for order: {}", order.getOrderNumber());
    restoreStock(order);
  }


  /**
   * Helper Method - Restore stock (private).
   */
  private void restoreStock(Order order) {
    for (OrderItem item : order.getItems()) {
      int result = productRepository.increaseStock(item.getProductId(), item.getQuantity());
      if (result > 0) {
        log.debug("Restored {} units of stock for product {}",
            item.getQuantity(), item.getProductId());
      } else {
        log.warn("Failed to restore stock for product {}. Product may have been deleted.",
            item.getProductId());
      }
    }
  }
}