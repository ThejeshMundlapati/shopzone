package com.shopzone.orderservice.kafka;

import com.shopzone.common.config.KafkaTopicConfig;
import com.shopzone.common.event.*;
import com.shopzone.orderservice.model.Order;
import com.shopzone.orderservice.model.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Publishes order lifecycle events to Kafka.
 * Key = orderNumber (ensures all events for one order go to the same partition).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // === Order Events ===

    public void publishOrderCreated(Order order) {
        OrderEvent event = buildOrderEvent("ORDER_CREATED", order);
        send(KafkaTopicConfig.ORDER_EVENTS_TOPIC, order.getOrderNumber(), event);
        log.info("Published ORDER_CREATED for {}", order.getOrderNumber());
    }

    public void publishOrderConfirmed(Order order) {
        OrderEvent event = buildOrderEvent("ORDER_CONFIRMED", order);
        send(KafkaTopicConfig.ORDER_EVENTS_TOPIC, order.getOrderNumber(), event);
        log.info("Published ORDER_CONFIRMED for {}", order.getOrderNumber());

        // Also request a confirmation email
        publishNotification("ORDER_CONFIRMED", order, null, null, null);
    }

    public void publishOrderShipped(Order order) {
        OrderEvent event = buildOrderEvent("ORDER_SHIPPED", order);
        event.setTrackingNumber(order.getTrackingNumber());
        event.setShippingCarrier(order.getShippingCarrier());
        send(KafkaTopicConfig.ORDER_EVENTS_TOPIC, order.getOrderNumber(), event);
        log.info("Published ORDER_SHIPPED for {}", order.getOrderNumber());

        // Also request a shipping email
        publishNotification("ORDER_SHIPPED", order, order.getTrackingNumber(),
                order.getShippingCarrier(), null);
    }

    public void publishOrderDelivered(Order order) {
        OrderEvent event = buildOrderEvent("ORDER_DELIVERED", order);
        send(KafkaTopicConfig.ORDER_EVENTS_TOPIC, order.getOrderNumber(), event);
        log.info("Published ORDER_DELIVERED for {}", order.getOrderNumber());

        publishNotification("ORDER_DELIVERED", order, null, null, null);
    }

    public void publishOrderCancelled(Order order, String reason) {
        OrderEvent event = buildOrderEvent("ORDER_CANCELLED", order);
        event.setCancellationReason(reason);
        event.setCancelledBy(order.getCancelledBy());
        send(KafkaTopicConfig.ORDER_EVENTS_TOPIC, order.getOrderNumber(), event);
        log.info("Published ORDER_CANCELLED for {}", order.getOrderNumber());

        publishNotification("ORDER_CANCELLED", order, null, null, reason);
    }

    // === Notification Events ===

    private void publishNotification(String type, Order order,
                                     String trackingNumber, String shippingCarrier,
                                     String cancellationReason) {
        NotificationEvent notif = NotificationEvent.builder()
                .notificationType(type)
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .userEmail(order.getUserEmail())
                .userFullName(order.getUserFullName())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .trackingNumber(trackingNumber)
                .shippingCarrier(shippingCarrier)
                .cancellationReason(cancellationReason)
                .build();
        send(KafkaTopicConfig.NOTIFICATION_EVENTS_TOPIC, order.getOrderNumber(), notif);
        log.debug("Published notification {} for {}", type, order.getOrderNumber());
    }

    // === Helpers ===

    private OrderEvent buildOrderEvent(String eventType, Order order) {
        return OrderEvent.builder()
                .eventType(eventType)
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .orderNumber(order.getOrderNumber())
                .orderId(order.getId())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .userFullName(order.getUserFullName())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingCost(order.getShippingCost())
                .totalAmount(order.getTotalAmount())
                .stripePaymentIntentId(order.getStripePaymentIntentId())
                .items(order.getItems().stream()
                        .map(this::toItemEvent)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderItemEvent toItemEvent(OrderItem item) {
        return OrderItemEvent.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    private void send(String topic, String key, Object event) {
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send event to {} with key {}: {}", topic, key, ex.getMessage());
                    }
                });
    }
}
