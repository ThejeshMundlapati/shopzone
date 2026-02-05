package com.shopzone.model;

import com.shopzone.model.enums.OrderStatus;
import com.shopzone.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user_id", columnList = "user_id"),
    @Index(name = "idx_order_number", columnList = "order_number"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created_at", columnList = "created_at"),
    @Index(name = "idx_order_payment_intent", columnList = "stripe_payment_intent_id")
})
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(name = "order_number", unique = true, nullable = false, length = 20)
  private String orderNumber;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "user_email", nullable = false)
  private String userEmail;

  @Column(name = "user_full_name")
  private String userFullName;


  @Column(name = "shipping_address_id")
  private String shippingAddressId;

  @Embedded
  private AddressSnapshot shippingAddress;


  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  @Builder.Default
  private List<OrderItem> items = new ArrayList<>();


  @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
  private BigDecimal subtotal;

  @Column(name = "tax_rate", precision = 5, scale = 4)
  private BigDecimal taxRate;

  @Column(name = "tax_amount", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(name = "shipping_cost", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal shippingCost = BigDecimal.ZERO;

  @Column(name = "discount_amount", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
  private BigDecimal totalAmount;


  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private OrderStatus status = OrderStatus.PENDING;


  @Column(name = "payment_id")
  private String paymentId;

  @Column(name = "payment_method")
  private String paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", length = 20)
  @Builder.Default
  private PaymentStatus paymentStatus = PaymentStatus.PENDING;

  /**
   * Stripe Payment Intent ID for this order.
   */
  @Column(name = "stripe_payment_intent_id")
  private String stripePaymentIntentId;

  /**
   * Stripe Charge ID after successful payment.
   */
  @Column(name = "stripe_charge_id")
  private String stripeChargeId;

  /**
   * Client secret for frontend payment confirmation.
   * Transient - not stored, only returned in response.
   */
  @Transient
  private String paymentClientSecret;

  /**
   * Receipt URL from Stripe.
   */
  @Column(name = "receipt_url", length = 500)
  private String receiptUrl;

  /**
   * Amount refunded (if any).
   */
  @Column(name = "amount_refunded", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal amountRefunded = BigDecimal.ZERO;


  @Column(name = "customer_notes", length = 500)
  private String customerNotes;

  @Column(name = "admin_notes", length = 1000)
  private String adminNotes;

  @Column(name = "tracking_number")
  private String trackingNumber;

  @Column(name = "shipping_carrier")
  private String shippingCarrier;


  @Column(name = "cancellation_reason", length = 500)
  private String cancellationReason;

  @Column(name = "cancelled_by", length = 10)
  private String cancelledBy;


  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "paid_at")
  private LocalDateTime paidAt;

  @Column(name = "confirmed_at")
  private LocalDateTime confirmedAt;

  @Column(name = "shipped_at")
  private LocalDateTime shippedAt;

  @Column(name = "delivered_at")
  private LocalDateTime deliveredAt;

  @Column(name = "cancelled_at")
  private LocalDateTime cancelledAt;


  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }


  public int getTotalItemCount() {
    return items.stream().mapToInt(OrderItem::getQuantity).sum();
  }

  public int getUniqueItemCount() {
    return items.size();
  }

  public BigDecimal getTotalItemSavings() {
    return items.stream()
        .map(OrderItem::getSavings)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public boolean canCancel() {
    return status.isCancellable();
  }

  public void updateStatus(OrderStatus newStatus) {
    this.status = newStatus;
    switch (newStatus) {
      case CONFIRMED -> this.confirmedAt = LocalDateTime.now();
      case SHIPPED -> this.shippedAt = LocalDateTime.now();
      case DELIVERED -> this.deliveredAt = LocalDateTime.now();
      case CANCELLED -> this.cancelledAt = LocalDateTime.now();
      default -> {}
    }
  }

  public void addItem(OrderItem item) {
    items.add(item);
  }


  /**
   * Record successful payment.
   */
  public void recordPayment(String chargeId, String receiptUrl) {
    this.stripeChargeId = chargeId;
    this.receiptUrl = receiptUrl;
    this.paymentStatus = PaymentStatus.PAID;
    this.paidAt = LocalDateTime.now();
    if (this.status == OrderStatus.PENDING) {
      this.status = OrderStatus.CONFIRMED;
      this.confirmedAt = LocalDateTime.now();
    }
  }

  /**
   * Record failed payment.
   */
  public void recordPaymentFailure() {
    this.paymentStatus = PaymentStatus.FAILED;
  }

  /**
   * Record refund.
   */
  public void recordRefund(BigDecimal refundAmount) {
    this.amountRefunded = this.amountRefunded.add(refundAmount);
    if (this.amountRefunded.compareTo(this.totalAmount) >= 0) {
      this.paymentStatus = PaymentStatus.REFUNDED;
      this.status = OrderStatus.REFUNDED;
    } else {
      this.paymentStatus = PaymentStatus.PARTIALLY_REFUNDED;
    }
  }

  /**
   * Check if order can be refunded.
   */
  public boolean canRefund() {
    return paymentStatus != null &&
        paymentStatus.canRefund() &&
        totalAmount.subtract(amountRefunded).compareTo(BigDecimal.ZERO) > 0;
  }

  /**
   * Get refundable amount.
   */
  public BigDecimal getRefundableAmount() {
    return totalAmount.subtract(amountRefunded);
  }
}