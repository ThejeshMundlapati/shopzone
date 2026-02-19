package com.shopzone.model.enums;

public enum EmailType {
  WELCOME("Welcome to ShopZone"),
  EMAIL_VERIFICATION("Verify Your Email"),
  PASSWORD_RESET("Password Reset Request"),
  ORDER_CONFIRMATION("Order Confirmation"),
  ORDER_SHIPPED("Your Order Has Shipped"),
  ORDER_DELIVERED("Your Order Has Been Delivered"),
  ORDER_CANCELLED("Order Cancellation"),
  REFUND_PROCESSED("Refund Processed"),
  PAYMENT_FAILED("Payment Failed");

  private final String defaultSubject;

  EmailType(String defaultSubject) {
    this.defaultSubject = defaultSubject;
  }

  public String getDefaultSubject() {
    return defaultSubject;
  }
}