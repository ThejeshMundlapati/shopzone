package com.shopzone.orderservice.service;

import com.shopzone.common.exception.*;
import com.shopzone.orderservice.client.*;
import com.shopzone.orderservice.config.OrderConfig;
import com.shopzone.orderservice.dto.request.*;
import com.shopzone.orderservice.dto.response.*;
import com.shopzone.orderservice.model.*;
import com.shopzone.orderservice.model.enums.*;
import com.shopzone.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final NotificationClient notificationClient;
    private final OrderConfig orderConfig;

    // === User order operations ===
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getUserOrders(String userId, OrderStatus status, Pageable pageable) {
        Page<Order> orders = status != null
            ? orderRepository.findByUserIdAndStatus(userId, status, pageable)
            : orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return orders.map(OrderSummaryResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderForUser(String userId, String orderNumber) {
        Order order = findByOrderNumber(orderNumber);
        if (!order.getUserId().equals(userId)) throw new UnauthorizedException("Not your order");
        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String userId, String orderNumber, CancelOrderRequest request) {
        Order order = findByOrderNumber(orderNumber);
        if (!order.getUserId().equals(userId)) throw new UnauthorizedException("Not your order");
        if (!order.canCancel()) throw new BadRequestException("Cannot cancel: " + order.getStatus().getDisplayName());

        LocalDateTime deadline = order.getCreatedAt().plusHours(orderConfig.getCancellationWindowHours());
        if (LocalDateTime.now().isAfter(deadline)) throw new BadRequestException("Cancellation window expired");
        if (order.getPaymentStatus() == PaymentStatus.PAID) throw new BadRequestException("Paid orders need refund instead");

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(request.getReason());
        order.setCancelledBy("USER");
        order.setPaymentStatus(PaymentStatus.CANCELLED);
        restoreStock(order);
        order = orderRepository.save(order);

        notificationClient.sendOrderCancelled(orderNumber, order.getUserEmail(), order.getUserFullName(), request.getReason());
        return OrderResponse.fromEntity(order);
    }

    // === Admin operations ===
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getAllOrders(OrderStatus status, PaymentStatus paymentStatus,
            LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return orderRepository.findWithFilters(status, paymentStatus, start, end, pageable)
            .map(OrderSummaryResponse::fromEntity);
    }

    @Transactional
    public OrderResponse updateOrderStatus(String orderNumber, UpdateOrderStatusRequest request) {
        Order order = findByOrderNumber(orderNumber);
        OrderStatus newStatus = request.getStatus();
        if (!order.getStatus().canTransitionTo(newStatus))
            throw new BadRequestException("Invalid transition: " + order.getStatus() + " -> " + newStatus);

        if (newStatus == OrderStatus.SHIPPED) {
            if (request.getTrackingNumber() == null || request.getTrackingNumber().isBlank())
                throw new BadRequestException("Tracking number required for shipping");
            order.setTrackingNumber(request.getTrackingNumber());
            order.setShippingCarrier(request.getShippingCarrier());
        }
        if (newStatus == OrderStatus.CANCELLED) { order.setCancelledBy("ADMIN"); restoreStock(order); }
        order.updateStatus(newStatus);
        if (request.getAdminNotes() != null && !request.getAdminNotes().isBlank()) {
            String existing = order.getAdminNotes() != null ? order.getAdminNotes() + "\n" : "";
            order.setAdminNotes(existing + "[" + LocalDateTime.now() + "] " + request.getAdminNotes());
        }
        order = orderRepository.save(order);

        // Send notification
        switch (newStatus) {
            case CONFIRMED -> notificationClient.sendOrderConfirmation(orderNumber, order.getUserEmail(), order.getUserFullName());
            case SHIPPED -> notificationClient.sendOrderShipped(orderNumber, order.getUserEmail(), order.getUserFullName(),
                request.getTrackingNumber(), request.getShippingCarrier());
            case DELIVERED -> notificationClient.sendOrderDelivered(orderNumber, order.getUserEmail(), order.getUserFullName());
            case CANCELLED -> notificationClient.sendOrderCancelled(orderNumber, order.getUserEmail(), order.getUserFullName(), "Admin cancelled");
            default -> {}
        }
        return OrderResponse.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> searchOrders(String query, Pageable pageable) {
        return orderRepository.searchOrders(query, pageable).map(OrderSummaryResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public OrderStatsResponse getOrderStats() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime week = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime month = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        Map<OrderStatus, Long> byStatus = new HashMap<>();
        orderRepository.getOrderCountsByStatus().forEach(r -> byStatus.put((OrderStatus)r[0], (Long)r[1]));

        long total = orderRepository.count();
        BigDecimal revenue = orderRepository.getTotalRevenue();
        if (revenue == null) revenue = BigDecimal.ZERO;

        return OrderStatsResponse.builder()
            .totalOrders(total).ordersByStatus(byStatus).totalRevenue(revenue)
            .averageOrderValue(total > 0 ? revenue.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
            .cancellationRate(total > 0 ? BigDecimal.valueOf(byStatus.getOrDefault(OrderStatus.CANCELLED, 0L))
                .multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
            .ordersToday(orderRepository.countByCreatedAtAfter(today))
            .ordersThisWeek(orderRepository.countByCreatedAtAfter(week))
            .ordersThisMonth(orderRepository.countByCreatedAtAfter(month))
            .pendingOrders(byStatus.getOrDefault(OrderStatus.PENDING, 0L))
            .processingOrders(byStatus.getOrDefault(OrderStatus.PROCESSING, 0L))
            .shippedOrders(byStatus.getOrDefault(OrderStatus.SHIPPED, 0L))
            .deliveredOrders(byStatus.getOrDefault(OrderStatus.DELIVERED, 0L))
            .build();
    }

    // === Internal methods (called by Payment Service via InternalOrderController) ===
    public Order findById(String id) { return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found")); }
    public Order findByOrderNumber(String num) { return orderRepository.findByOrderNumberWithItems(num).orElseThrow(() -> new ResourceNotFoundException("Order not found: " + num)); }
    public Order save(Order order) { return orderRepository.save(order); }
    public long countByUserId(String userId) { return orderRepository.countByUserId(userId); }

    public void reduceStockForOrder(Order order) {
        order.getItems().forEach(item -> {
            if (!productClient.reduceStock(item.getProductId(), item.getQuantity()))
                log.error("Failed to reduce stock for product {} in order {}", item.getProductId(), order.getOrderNumber());
        });
    }

    public void restoreStockForOrder(Order order) { restoreStock(order); }

    private void restoreStock(Order order) {
        order.getItems().forEach(item -> productClient.increaseStock(item.getProductId(), item.getQuantity()));
    }
}
