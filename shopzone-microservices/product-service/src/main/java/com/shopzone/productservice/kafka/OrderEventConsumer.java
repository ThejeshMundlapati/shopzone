package com.shopzone.productservice.kafka;

import com.shopzone.common.config.KafkaTopicConfig;
import com.shopzone.common.event.OrderEvent;
import com.shopzone.common.event.OrderItemEvent;
import com.shopzone.productservice.model.Product;
import com.shopzone.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Listens to order events and manages stock accordingly.
 *
 * ORDER_CREATED  → reserve stock (reduce) for each item
 * ORDER_CANCELLED → restore stock (increase) for each item
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final ProductRepository productRepository;
    private final StockEventProducer stockEventProducer;

    @KafkaListener(
            topics = KafkaTopicConfig.ORDER_EVENTS_TOPIC,
            groupId = "product-service-group",
            containerFactory = "orderEventListenerFactory"
    )
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: {} for order {}", event.getEventType(), event.getOrderNumber());

        try {
            switch (event.getEventType()) {
                case "ORDER_CREATED" -> reserveStock(event);
                case "ORDER_CANCELLED" -> restoreStock(event);
                default -> log.debug("Ignoring order event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error handling order event {} for {}: {}",
                    event.getEventType(), event.getOrderNumber(), e.getMessage(), e);
        }
    }

    /**
     * Reserve stock for all items in the order.
     * If ANY item has insufficient stock, fail the entire reservation and publish STOCK_RESERVE_FAILED.
     * This is an atomic check — either all items succeed or none do.
     */
    private void reserveStock(OrderEvent event) {
        if (event.getItems() == null || event.getItems().isEmpty()) {
            log.warn("ORDER_CREATED for {} has no items. Skipping stock reservation.", event.getOrderNumber());
            return;
        }

        // Phase 1: Check all items have sufficient stock
        for (OrderItemEvent item : event.getItems()) {
            Optional<Product> productOpt = productRepository.findById(item.getProductId());
            if (productOpt.isEmpty()) {
                log.error("Product {} not found for order {}", item.getProductId(), event.getOrderNumber());
                stockEventProducer.publishStockReserveFailed(
                        event.getOrderNumber(), event.getOrderId(),
                        item.getProductId(), item.getProductName(),
                        item.getQuantity(), 0);
                return;
            }

            Product product = productOpt.get();
            if (product.getStock() == null || product.getStock() < item.getQuantity()) {
                int available = product.getStock() != null ? product.getStock() : 0;
                log.warn("Insufficient stock for product {} (need {}, have {}) in order {}",
                        product.getName(), item.getQuantity(), available, event.getOrderNumber());
                stockEventProducer.publishStockReserveFailed(
                        event.getOrderNumber(), event.getOrderId(),
                        item.getProductId(), product.getName(),
                        item.getQuantity(), available);
                return;
            }
        }

        // Phase 2: All checks passed — actually reduce stock
        for (OrderItemEvent item : event.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElseThrow();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
            log.debug("Reserved {} units of {} for order {}",
                    item.getQuantity(), product.getName(), event.getOrderNumber());
        }

        stockEventProducer.publishStockReserved(
                event.getOrderNumber(), event.getOrderId(), event.getItems());
        log.info("Stock reserved for all {} items in order {}",
                event.getItems().size(), event.getOrderNumber());
    }

    /**
     * Restore stock for all items when an order is cancelled.
     * This is the compensation step for stock reservation.
     */
    private void restoreStock(OrderEvent event) {
        if (event.getItems() == null || event.getItems().isEmpty()) {
            log.warn("ORDER_CANCELLED for {} has no items. Skipping stock restore.", event.getOrderNumber());
            return;
        }

        for (OrderItemEvent item : event.getItems()) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                int currentStock = product.getStock() != null ? product.getStock() : 0;
                product.setStock(currentStock + item.getQuantity());
                productRepository.save(product);
                log.debug("Restored {} units of {} for cancelled order {}",
                        item.getQuantity(), product.getName(), event.getOrderNumber());
            });
        }

        stockEventProducer.publishStockRestored(
                event.getOrderNumber(), event.getOrderId(), event.getItems());
        log.info("Stock restored for {} items in cancelled order {}",
                event.getItems().size(), event.getOrderNumber());
    }
}
