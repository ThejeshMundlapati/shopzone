package com.shopzone.searchservice.kafka;

import com.shopzone.common.config.KafkaTopicConfig;
import com.shopzone.common.event.OrderEvent;
import com.shopzone.common.event.OrderItemEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to order events and logs when products need re-syncing.
 *
 * When an order is confirmed or cancelled, product stock levels change.
 * This consumer logs the affected product IDs. The actual re-sync happens
 * via the existing Product Service → Search Service sync mechanism
 * (SearchSyncClient calls from Product Service).
 *
 * In a production system, this would call the search service's own
 * indexing logic directly. For now, Product Service already syncs
 * to Elasticsearch when stock changes, so this is a secondary notification.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSyncConsumer {

    @KafkaListener(
        topics = KafkaTopicConfig.ORDER_EVENTS_TOPIC,
        groupId = "search-service-group",
        containerFactory = "orderEventListenerFactory"
    )
    public void handleOrderEvent(OrderEvent event) {
        // Only care about events that change stock levels
        if (!"ORDER_CONFIRMED".equals(event.getEventType()) &&
            !"ORDER_CANCELLED".equals(event.getEventType())) {
            return;
        }

        log.info("Search Service received {} for order {} — products may need re-indexing",
            event.getEventType(), event.getOrderNumber());

        if (event.getItems() != null) {
            for (OrderItemEvent item : event.getItems()) {
                log.info("Product {} ({}) may have updated stock — should be re-indexed",
                    item.getProductId(), item.getProductName());
            }
        }
    }
}