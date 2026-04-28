package com.shopzone.orderservice.model.enums;

import java.util.Set;

public enum OrderStatus {
    PENDING("Pending", true, Set.of("CONFIRMED", "CANCELLED")),
    CONFIRMED("Confirmed", true, Set.of("PROCESSING", "CANCELLED")),
    PROCESSING("Processing", true, Set.of("SHIPPED", "CANCELLED")),
    SHIPPED("Shipped", false, Set.of("DELIVERED", "RETURNED")),
    DELIVERED("Delivered", false, Set.of("RETURNED")),
    RETURNED("Returned", false, Set.of("REFUNDED")),
    CANCELLED("Cancelled", false, Set.of()),
    REFUNDED("Refunded", false, Set.of());

    private final String displayName;
    private final boolean cancellable;
    private final Set<String> allowedTransitions;

    OrderStatus(String displayName, boolean cancellable, Set<String> transitions) {
        this.displayName = displayName; this.cancellable = cancellable; this.allowedTransitions = transitions;
    }
    public String getDisplayName() { return displayName; }
    public boolean isCancellable() { return cancellable; }
    public boolean canTransitionTo(OrderStatus s) { return allowedTransitions.contains(s.name()); }
}
