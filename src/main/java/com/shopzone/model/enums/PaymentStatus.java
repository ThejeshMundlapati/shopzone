package com.shopzone.model.enums;

/**
 * Represents the payment status of an order.
 * Tracks the state of payment processing.
 */
public enum PaymentStatus {

  /**
   * Payment not yet initiated or awaiting payment.
   * Initial state for new orders.
   */
  PENDING("Pending", "Awaiting payment"),

  /**
   * Payment is being processed.
   * Waiting for payment gateway confirmation.
   */
  PROCESSING("Processing", "Payment is being processed"),

  /**
   * Payment successfully completed.
   * Funds have been captured.
   */
  PAID("Paid", "Payment successful"),

  /**
   * Payment attempt failed.
   * Card declined, insufficient funds, etc.
   */
  FAILED("Failed", "Payment failed"),

  /**
   * Full refund has been issued.
   * All funds returned to customer.
   */
  REFUNDED("Refunded", "Payment refunded"),

  /**
   * Partial refund has been issued.
   * Some funds returned to customer.
   */
  PARTIALLY_REFUNDED("Partially Refunded", "Partial refund issued");

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
   * Check if payment has been completed successfully.
   */
  public boolean isPaid() {
    return this == PAID || this == PARTIALLY_REFUNDED;
  }

  /**
   * Check if payment is still in progress.
   */
  public boolean isInProgress() {
    return this == PENDING || this == PROCESSING;
  }

  /**
   * Check if refund is possible from this status.
   */
  public boolean isRefundable() {
    return this == PAID || this == PARTIALLY_REFUNDED;
  }
}