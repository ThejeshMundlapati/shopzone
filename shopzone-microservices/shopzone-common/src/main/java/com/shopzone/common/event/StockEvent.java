package com.shopzone.common.event;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Published by Product Service to shopzone.stock.events topic.
 *
 * Event types:
 *   STOCK_RESERVED      — stock successfully reserved for order
 *   STOCK_RESERVE_FAILED — insufficient stock for one or more items
 *   STOCK_RESTORED       — stock restored after cancellation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventType;
    private String eventId;
    private LocalDateTime timestamp;

    private String orderNumber;
    private String orderId;

    // Which items were affected
    private List<OrderItemEvent> items;

    // Set when STOCK_RESERVE_FAILED — which product had insufficient stock
    private String failedProductId;
    private String failedProductName;
    private int requestedQuantity;
    private int availableQuantity;
}
