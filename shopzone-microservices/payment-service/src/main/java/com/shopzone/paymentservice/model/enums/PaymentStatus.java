package com.shopzone.paymentservice.model.enums;
public enum PaymentStatus {
    PENDING, AWAITING_PAYMENT, PROCESSING, PAID, FAILED, CANCELLED, REFUNDED, PARTIALLY_REFUNDED, REFUND_PENDING, REQUIRES_ACTION;
    public boolean isPaid() { return this == PAID || this == PARTIALLY_REFUNDED; }
    public boolean canRefund() { return this == PAID || this == PARTIALLY_REFUNDED; }
    public boolean isTerminal() { return this == PAID || this == FAILED || this == CANCELLED || this == REFUNDED || this == PARTIALLY_REFUNDED; }
}
