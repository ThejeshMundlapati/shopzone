package com.shopzone.common.event;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Published to shopzone.notification.events topic.
 *
 * Notification types:
 *   ORDER_RECEIVED     — new order placed
 *   ORDER_CONFIRMED    — payment received
 *   ORDER_SHIPPED      — order shipped with tracking
 *   ORDER_DELIVERED    — order delivered
 *   ORDER_CANCELLED    — order cancelled
 *   PAYMENT_RECEIVED   — payment confirmation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String notificationType;
    private String eventId;
    private LocalDateTime timestamp;

    // Recipient
    private String userEmail;
    private String userFullName;

    // Order context
    private String orderNumber;
    private BigDecimal totalAmount;

    // Shipping (for SHIPPED notifications)
    private String trackingNumber;
    private String shippingCarrier;

    // Cancellation (for CANCELLED notifications)
    private String cancellationReason;

    // Extra data (flexible key-value for future notification types)
    private Map<String, String> metadata;
}
