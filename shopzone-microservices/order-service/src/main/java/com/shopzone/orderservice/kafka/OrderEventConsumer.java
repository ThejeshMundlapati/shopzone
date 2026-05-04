package com.shopzone.orderservice.kafka;

import com.shopzone.common.config.KafkaTopicConfig;
import com.shopzone.common.event.*;
import com.shopzone.orderservice.saga.OrderSagaManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes events from other services to drive the order saga.
 *
 * Listens to:
 *   - shopzone.stock.events   → stock reservation results
 *   - shopzone.payment.events → payment results
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderSagaManager sagaManager;

    @KafkaListener(
            topics = KafkaTopicConfig.STOCK_EVENTS_TOPIC,
            groupId = "order-service-stock-group",
            containerFactory = "stockEventListenerFactory"
    )
    public void handleStockEvent(StockEvent event) {
        log.info("Received stock event: {} for order {}", event.getEventType(), event.getOrderNumber());

        try {
            switch (event.getEventType()) {
                case "STOCK_RESERVED" -> sagaManager.onStockReserved(event);
                case "STOCK_RESERVE_FAILED" -> sagaManager.onStockReserveFailed(event);
                case "STOCK_RESTORED" -> log.info("Stock restored for order {}", event.getOrderNumber());
                default -> log.warn("Unknown stock event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error handling stock event for order {}: {}", event.getOrderNumber(), e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = KafkaTopicConfig.PAYMENT_EVENTS_TOPIC,
            groupId = "order-service-payment-group",
            containerFactory = "paymentEventListenerFactory"
    )
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: {} for order {}", event.getEventType(), event.getOrderNumber());

        try {
            switch (event.getEventType()) {
                case "PAYMENT_CREATED" -> sagaManager.onPaymentCreated(event);
                case "PAYMENT_SUCCESS" -> sagaManager.onPaymentSuccess(event);
                case "PAYMENT_FAILED" -> sagaManager.onPaymentFailed(event);
                default -> log.warn("Unknown payment event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error handling payment event for order {}: {}", event.getOrderNumber(), e.getMessage(), e);
        }
    }
}
