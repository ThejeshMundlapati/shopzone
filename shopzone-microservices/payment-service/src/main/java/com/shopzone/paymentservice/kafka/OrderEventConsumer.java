package com.shopzone.paymentservice.kafka;

import com.shopzone.common.config.KafkaTopicConfig;
import com.shopzone.common.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to order events.
 *
 * NOTE: Payment intent creation is still done via REST (CheckoutService calls PaymentService
 * synchronously) because the frontend needs the clientSecret immediately to render Stripe
 * Elements. This consumer handles events that DON'T need a synchronous response:
 *
 *   ORDER_CANCELLED → Could trigger automatic refund in the future
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    @KafkaListener(
            topics = KafkaTopicConfig.ORDER_EVENTS_TOPIC,
            groupId = "payment-service-group",
            containerFactory = "orderEventListenerFactory"
    )
    public void handleOrderEvent(OrderEvent event) {
        log.info("Payment Service received order event: {} for order {}",
                event.getEventType(), event.getOrderNumber());

        try {
            switch (event.getEventType()) {
                case "ORDER_CANCELLED" -> handleOrderCancelled(event);
                default -> log.debug("Payment Service ignoring order event: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error handling order event {} for {}: {}",
                    event.getEventType(), event.getOrderNumber(), e.getMessage(), e);
        }
    }

    private void handleOrderCancelled(OrderEvent event) {
        // Log for now. In a production system, this would trigger automatic refund
        // if payment was already processed. For ShopZone, refunds are admin-initiated.
        log.info("Order {} cancelled. Payment Service notified. " +
                "Refund will be processed if admin initiates it.", event.getOrderNumber());
    }
}
