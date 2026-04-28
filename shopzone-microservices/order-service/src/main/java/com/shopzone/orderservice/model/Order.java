package com.shopzone.orderservice.model;

import com.shopzone.orderservice.model.enums.OrderStatus;
import com.shopzone.orderservice.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "orders", indexes = {
    @Index(name = "idx_order_user_id", columnList = "user_id"),
    @Index(name = "idx_order_number", columnList = "order_number"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created_at", columnList = "created_at"),
    @Index(name = "idx_order_payment_intent", columnList = "stripe_payment_intent_id")
})
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(name = "order_number", unique = true, nullable = false, length = 20) private String orderNumber;
    @Column(name = "user_id", nullable = false) private String userId;
    @Column(name = "user_email", nullable = false) private String userEmail;
    @Column(name = "user_full_name") private String userFullName;
    @Column(name = "shipping_address_id") private String shippingAddressId;
    @Embedded private AddressSnapshot shippingAddress;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(precision = 10, scale = 2, nullable = false) private BigDecimal subtotal;
    @Column(name = "tax_rate", precision = 5, scale = 4) private BigDecimal taxRate;
    @Column(name = "tax_amount", precision = 10, scale = 2) @Builder.Default private BigDecimal taxAmount = BigDecimal.ZERO;
    @Column(name = "shipping_cost", precision = 10, scale = 2) @Builder.Default private BigDecimal shippingCost = BigDecimal.ZERO;
    @Column(name = "discount_amount", precision = 10, scale = 2) @Builder.Default private BigDecimal discountAmount = BigDecimal.ZERO;
    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false) private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "payment_id") private String paymentId;
    @Column(name = "payment_method") private String paymentMethod;
    @Enumerated(EnumType.STRING) @Column(name = "payment_status") @Builder.Default private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    @Column(name = "stripe_payment_intent_id") private String stripePaymentIntentId;
    @Column(name = "stripe_charge_id") private String stripeChargeId;
    @Transient private String paymentClientSecret;
    @Column(name = "receipt_url", length = 500) private String receiptUrl;
    @Column(name = "amount_refunded", precision = 10, scale = 2) @Builder.Default private BigDecimal amountRefunded = BigDecimal.ZERO;

    @Column(name = "customer_notes", length = 500) private String customerNotes;
    @Column(name = "admin_notes", length = 1000) private String adminNotes;
    @Column(name = "tracking_number") private String trackingNumber;
    @Column(name = "shipping_carrier") private String shippingCarrier;
    @Column(name = "cancellation_reason", length = 500) private String cancellationReason;
    @Column(name = "cancelled_by", length = 10) private String cancelledBy;

    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "paid_at") private LocalDateTime paidAt;
    @Column(name = "confirmed_at") private LocalDateTime confirmedAt;
    @Column(name = "shipped_at") private LocalDateTime shippedAt;
    @Column(name = "delivered_at") private LocalDateTime deliveredAt;
    @Column(name = "cancelled_at") private LocalDateTime cancelledAt;

    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public boolean canCancel() { return status.isCancellable(); }

    public void updateStatus(OrderStatus s) {
        this.status = s;
        switch (s) {
            case CONFIRMED -> confirmedAt = LocalDateTime.now();
            case SHIPPED -> shippedAt = LocalDateTime.now();
            case DELIVERED -> deliveredAt = LocalDateTime.now();
            case CANCELLED -> cancelledAt = LocalDateTime.now();
            default -> {}
        }
    }

    public void recordPayment(String chargeId, String receiptUrl) {
        this.stripeChargeId = chargeId; this.receiptUrl = receiptUrl;
        this.paymentStatus = PaymentStatus.PAID; this.paidAt = LocalDateTime.now();
        if (this.status == OrderStatus.PENDING) { this.status = OrderStatus.CONFIRMED; this.confirmedAt = LocalDateTime.now(); }
    }

    public void recordPaymentFailure() { this.paymentStatus = PaymentStatus.FAILED; }

    public void recordRefund(BigDecimal amount) {
        this.amountRefunded = this.amountRefunded.add(amount);
        this.paymentStatus = amountRefunded.compareTo(totalAmount) >= 0 ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED;
        if (amountRefunded.compareTo(totalAmount) >= 0) this.status = OrderStatus.REFUNDED;
    }

    public BigDecimal getRefundableAmount() { return totalAmount.subtract(amountRefunded); }
    public int getTotalItemCount() { return items.stream().mapToInt(OrderItem::getQuantity).sum(); }
}
