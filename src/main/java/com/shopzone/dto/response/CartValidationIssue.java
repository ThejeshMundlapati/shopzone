package com.shopzone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a validation issue found during checkout validation.
 * Used to inform the user about problems with their cart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartValidationIssue {

  /**
   * Product ID with the issue.
   */
  private String productId;

  /**
   * Product name for display.
   */
  private String productName;

  /**
   * Type of issue detected.
   */
  private IssueType issueType;

  /**
   * Human-readable message describing the issue.
   */
  private String message;

  /**
   * Current available stock (for stock-related issues).
   */
  private Integer availableStock;

  /**
   * Requested quantity in cart.
   */
  private Integer requestedQuantity;

  /**
   * Old price (for price change issues).
   */
  private BigDecimal oldPrice;

  /**
   * New/current price (for price change issues).
   */
  private BigDecimal newPrice;

  /**
   * Types of validation issues that can occur.
   */
  public enum IssueType {
    /**
     * Product no longer exists in catalog.
     */
    PRODUCT_NOT_FOUND,

    /**
     * Product exists but is no longer active/available.
     */
    PRODUCT_UNAVAILABLE,

    /**
     * Product is completely out of stock.
     */
    OUT_OF_STOCK,

    /**
     * Not enough stock to fulfill requested quantity.
     */
    INSUFFICIENT_STOCK,

    /**
     * Product price has changed since adding to cart.
     */
    PRICE_CHANGED,

    /**
     * Product is no longer on sale (discount removed).
     */
    DISCOUNT_EXPIRED
  }


  public static CartValidationIssue productNotFound(String productId, String productName) {
    return CartValidationIssue.builder()
        .productId(productId)
        .productName(productName)
        .issueType(IssueType.PRODUCT_NOT_FOUND)
        .message("Product '" + productName + "' is no longer available")
        .build();
  }

  public static CartValidationIssue productUnavailable(String productId, String productName) {
    return CartValidationIssue.builder()
        .productId(productId)
        .productName(productName)
        .issueType(IssueType.PRODUCT_UNAVAILABLE)
        .message("Product '" + productName + "' is currently unavailable")
        .build();
  }

  public static CartValidationIssue outOfStock(String productId, String productName) {
    return CartValidationIssue.builder()
        .productId(productId)
        .productName(productName)
        .issueType(IssueType.OUT_OF_STOCK)
        .availableStock(0)
        .message("Product '" + productName + "' is out of stock")
        .build();
  }

  public static CartValidationIssue insufficientStock(String productId, String productName,
                                                      int available, int requested) {
    return CartValidationIssue.builder()
        .productId(productId)
        .productName(productName)
        .issueType(IssueType.INSUFFICIENT_STOCK)
        .availableStock(available)
        .requestedQuantity(requested)
        .message("Only " + available + " units of '" + productName +
            "' available (requested: " + requested + ")")
        .build();
  }

  public static CartValidationIssue priceChanged(String productId, String productName,
                                                 BigDecimal oldPrice, BigDecimal newPrice) {
    String direction = newPrice.compareTo(oldPrice) > 0 ? "increased" : "decreased";
    return CartValidationIssue.builder()
        .productId(productId)
        .productName(productName)
        .issueType(IssueType.PRICE_CHANGED)
        .oldPrice(oldPrice)
        .newPrice(newPrice)
        .message("Price of '" + productName + "' has " + direction +
            " from $" + oldPrice + " to $" + newPrice)
        .build();
  }
}