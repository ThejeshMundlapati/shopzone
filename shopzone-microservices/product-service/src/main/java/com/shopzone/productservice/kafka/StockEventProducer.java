package com.shopzone.productservice.kafka;

import com.shopzone.common.config.KafkaTopicConfig;
import com.shopzone.common.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStockReserved(String orderNumber, String orderId, List<OrderItemEvent> items) {
        StockEvent event = StockEvent.builder()
                .eventType("STOCK_RESERVED")
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .orderNumber(orderNumber)
                .orderId(orderId)
                .items(items)
                .build();

        kafkaTemplate.send(KafkaTopicConfig.STOCK_EVENTS_TOPIC, orderNumber, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) log.error("Failed to publish STOCK_RESERVED for {}: {}", orderNumber, ex.getMessage());
                });
        log.info("Published STOCK_RESERVED for order {}", orderNumber);
    }

    public void publishStockReserveFailed(String orderNumber, String orderId,
                                           String failedProductId, String failedProductName,
                                           int requestedQty, int availableQty) {
        StockEvent event = StockEvent.builder()
                .eventType("STOCK_RESERVE_FAILED")
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .orderNumber(orderNumber)
                .orderId(orderId)
                .failedProductId(failedProductId)
                .failedProductName(failedProductName)
                .requestedQuantity(requestedQty)
                .availableQuantity(availableQty)
                .build();

        kafkaTemplate.send(KafkaTopicConfig.STOCK_EVENTS_TOPIC, orderNumber, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) log.error("Failed to publish STOCK_RESERVE_FAILED for {}: {}", orderNumber, ex.getMessage());
                });
        log.info("Published STOCK_RESERVE_FAILED for order {} (product: {})", orderNumber, failedProductName);
    }

    public void publishStockRestored(String orderNumber, String orderId, List<OrderItemEvent> items) {
        StockEvent event = StockEvent.builder()
                .eventType("STOCK_RESTORED")
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .orderNumber(orderNumber)
                .orderId(orderId)
                .items(items)
                .build();

        kafkaTemplate.send(KafkaTopicConfig.STOCK_EVENTS_TOPIC, orderNumber, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) log.error("Failed to publish STOCK_RESTORED for {}: {}", orderNumber, ex.getMessage());
                });
        log.info("Published STOCK_RESTORED for order {}", orderNumber);
    }
}
