package com.shopzone.model.enums;

/**
 * Payment method types supported by the platform.
 */
public enum PaymentMethod {

  /**
   * Credit or debit card payment via Stripe.
   */
  CARD("Card", "Credit or Debit Card"),

  /**
   * Bank transfer/ACH payment.
   */
  BANK_TRANSFER("Bank Transfer", "Direct Bank Transfer"),

  /**
   * Digital wallet payment (Apple Pay, Google Pay).
   */
  WALLET("Digital Wallet", "Apple Pay, Google Pay, etc."),

  /**
   * Buy Now Pay Later services.
   */
  BNPL("BNPL", "Buy Now Pay Later"),

  /**
   * Cash on delivery (if supported).
   */
  COD("COD", "Cash on Delivery"),

  /**
   * Payment method not specified.
   */
  UNKNOWN("Unknown", "Payment method not determined");

  private final String displayName;
  private final String description;

  PaymentMethod(String displayName, String description) {
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
   * Map Stripe payment method type to our enum.
   */
  public static PaymentMethod fromStripeType(String stripeType) {
    if (stripeType == null) {
      return UNKNOWN;
    }

    return switch (stripeType.toLowerCase()) {
      case "card" -> CARD;
      case "us_bank_account", "sepa_debit", "bacs_debit" -> BANK_TRANSFER;
      case "apple_pay", "google_pay", "link" -> WALLET;
      case "klarna", "afterpay_clearpay", "affirm" -> BNPL;
      default -> UNKNOWN;
    };
  }
}