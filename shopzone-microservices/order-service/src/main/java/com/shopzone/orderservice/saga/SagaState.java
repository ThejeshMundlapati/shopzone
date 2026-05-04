package com.shopzone.orderservice.saga;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Persists the saga state for each order.
 * This ensures we can resume after a crash and prevent duplicate processing.
 *
 * States:
 *   STARTED          → order created, waiting for stock reservation
 *   STOCK_RESERVED   → stock reserved, waiting for payment creation
 *   PAYMENT_CREATED  → payment intent created, waiting for customer to pay
 *   PAYMENT_RECEIVED → payment successful, order being confirmed
 *   COMPLETED        → saga finished successfully
 *   COMPENSATING     → a step failed, undoing previous steps
 *   FAILED           → saga failed after compensation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "saga_states", indexes = {
        @Index(name = "idx_saga_order_number", columnList = "order_number", unique = true),
        @Index(name = "idx_saga_status", columnList = "status")
})
public class SagaState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "order_number", unique = true, nullable = false, length = 20)
    private String orderNumber;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "last_event_id", length = 50)
    private String lastEventId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === State constants ===
    public static final String STARTED = "STARTED";
    public static final String STOCK_RESERVED = "STOCK_RESERVED";
    public static final String PAYMENT_CREATED = "PAYMENT_CREATED";
    public static final String PAYMENT_RECEIVED = "PAYMENT_RECEIVED";
    public static final String COMPLETED = "COMPLETED";
    public static final String COMPENSATING = "COMPENSATING";
    public static final String FAILED = "FAILED";
}
