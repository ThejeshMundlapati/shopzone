package com.shopzone.common.event;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Published by Order Service to shopzone.order.events topic.
 *
 * Event types:
 *   ORDER_CREATED   — new order placed, triggers stock reservation + payment creation
 *   ORDER_CONFIRMED — payment received, order confirmed
 *   ORDER_SHIPPED   — admin shipped the order
 *   ORDER_DELIVERED  — order delivered
 *   ORDER_CANCELLED — order cancelled (user/admin/saga compensation)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventType;
    private String eventId;           // UUID for idempotency
    private LocalDateTime timestamp;

    // Order data
    private String orderNumber;
    private String orderId;
    private String userId;
    private String userEmail;
    private String userFullName;

    // Financial
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;

    // Items (needed by Product Service for stock operations)
    private List<OrderItemEvent> items;

    // Payment (set after payment is created)
    private String stripePaymentIntentId;

    // Shipping (set when status = SHIPPED)
    private String trackingNumber;
    private String shippingCarrier;

    // Cancellation (set when status = CANCELLED)
    private String cancellationReason;
    private String cancelledBy;
}
