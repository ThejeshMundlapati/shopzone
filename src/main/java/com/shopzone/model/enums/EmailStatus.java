package com.shopzone.model.enums;

public enum EmailStatus {
  PENDING("Pending"),
  SENT("Sent"),
  FAILED("Failed"),
  RETRYING("Retrying");

  private final String displayName;

  EmailStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}