package com.shopzone.model.enums;

/**
 * Payment status for orders.
 * Updated for Stripe integration with additional states.
 */
public enum PaymentStatus {

  /**
   * Payment not yet initiated.
   * Order created but payment process not started.
   */
  PENDING("Pending", "Payment not yet initiated"),

  /**
   * Payment Intent created, awaiting customer action.
   * Customer needs to complete payment on frontend.
   */
  AWAITING_PAYMENT("Awaiting Payment", "Waiting for customer to complete payment"),

  /**
   * Payment is being processed by Stripe.
   */
  PROCESSING("Processing", "Payment is being processed"),

  /**
   * Payment completed successfully.
   */
  PAID("Paid", "Payment completed successfully"),

  /**
   * Payment failed (declined, insufficient funds, etc.).
   */
  FAILED("Failed", "Payment failed"),

  /**
   * Payment was cancelled by user or system.
   */
  CANCELLED("Cancelled", "Payment was cancelled"),

  /**
   * Full refund has been processed.
   */
  REFUNDED("Refunded", "Full refund processed"),

  /**
   * Partial refund has been processed.
   */
  PARTIALLY_REFUNDED("Partially Refunded", "Partial refund processed"),

  /**
   * Refund is pending processing.
   */
  REFUND_PENDING("Refund Pending", "Refund is being processed"),

  /**
   * Payment requires additional authentication (3D Secure).
   */
  REQUIRES_ACTION("Requires Action", "Additional authentication required");

  private final String displayName;
  private final String description;

  PaymentStatus(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Check if this status indicates payment was successful.
   */
  public boolean isPaid() {
    return this == PAID || this == PARTIALLY_REFUNDED;
  }

  /**
   * Check if this status indicates payment is complete (terminal state for payment).
   */
  public boolean isTerminal() {
    return this == PAID || this == FAILED || this == CANCELLED ||
        this == REFUNDED || this == PARTIALLY_REFUNDED;
  }

  /**
   * Check if this status allows refund.
   */
  public boolean canRefund() {
    return this == PAID || this == PARTIALLY_REFUNDED;
  }

  /**
   * Check if payment can be retried.
   */
  public boolean canRetry() {
    return this == FAILED || this == CANCELLED || this == PENDING;
  }
}