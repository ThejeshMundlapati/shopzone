package com.shopzone.model.enums;

import java.util.Set;

public enum OrderStatus {
  PENDING("Pending", "Order placed, awaiting payment/confirmation", true, Set.of("CONFIRMED", "CANCELLED")),
  CONFIRMED("Confirmed", "Payment successful, order confirmed", true, Set.of("PROCESSING", "CANCELLED")),
  PROCESSING("Processing", "Order is being prepared", true, Set.of("SHIPPED", "CANCELLED")),
  SHIPPED("Shipped", "Order has been shipped", false, Set.of("DELIVERED", "RETURNED")),
  DELIVERED("Delivered", "Order delivered to customer", false, Set.of("RETURNED")),
  RETURNED("Returned", "Order returned by customer", false, Set.of("REFUNDED")),
  CANCELLED("Cancelled", "Order cancelled", false, Set.of()),
  REFUNDED("Refunded", "Payment refunded", false, Set.of());

  private final String displayName;
  private final String description;
  private final boolean cancellable;
  private final Set<String> allowedTransitions;

  OrderStatus(String displayName, String description, boolean cancellable, Set<String> allowedTransitions) {
    this.displayName = displayName;
    this.description = description;
    this.cancellable = cancellable;
    this.allowedTransitions = allowedTransitions;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  public boolean isCancellable() {
    return cancellable;
  }

  public boolean canTransitionTo(OrderStatus newStatus) {
    return allowedTransitions.contains(newStatus.name());
  }
}