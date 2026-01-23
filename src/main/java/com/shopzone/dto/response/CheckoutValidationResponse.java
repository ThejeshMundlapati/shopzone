package com.shopzone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for cart/checkout validation.
 * Provides detailed information about any issues preventing checkout.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutValidationResponse {

  /**
   * Whether the cart is valid for checkout.
   * True if no blocking issues found.
   */
  private boolean valid;

  /**
   * Whether there are warnings (non-blocking issues like price changes).
   */
  private boolean hasWarnings;

  /**
   * List of blocking issues that prevent checkout.
   */
  @Builder.Default
  private List<CartValidationIssue> errors = new ArrayList<>();

  /**
   * List of non-blocking warnings (user can still proceed).
   */
  @Builder.Default
  private List<CartValidationIssue> warnings = new ArrayList<>();

  /**
   * The validated cart (with updated information).
   */
  private CartResponse cart;

  /**
   * Summary message.
   */
  private String message;


  public static CheckoutValidationResponse valid(CartResponse cart) {
    return CheckoutValidationResponse.builder()
        .valid(true)
        .hasWarnings(false)
        .cart(cart)
        .message("Cart is ready for checkout")
        .build();
  }

  public static CheckoutValidationResponse validWithWarnings(CartResponse cart,
                                                             List<CartValidationIssue> warnings) {
    return CheckoutValidationResponse.builder()
        .valid(true)
        .hasWarnings(true)
        .warnings(warnings)
        .cart(cart)
        .message("Cart is ready for checkout with some changes")
        .build();
  }

  public static CheckoutValidationResponse invalid(List<CartValidationIssue> errors,
                                                   CartResponse cart) {
    return CheckoutValidationResponse.builder()
        .valid(false)
        .hasWarnings(false)
        .errors(errors)
        .cart(cart)
        .message("Cart has issues that need to be resolved")
        .build();
  }

  public static CheckoutValidationResponse invalid(List<CartValidationIssue> errors,
                                                   List<CartValidationIssue> warnings,
                                                   CartResponse cart) {
    return CheckoutValidationResponse.builder()
        .valid(false)
        .hasWarnings(!warnings.isEmpty())
        .errors(errors)
        .warnings(warnings)
        .cart(cart)
        .message("Cart has issues that need to be resolved")
        .build();
  }

  public static CheckoutValidationResponse emptyCart() {
    return CheckoutValidationResponse.builder()
        .valid(false)
        .hasWarnings(false)
        .message("Cart is empty")
        .build();
  }

  /**
   * Get total count of all issues (errors + warnings).
   */
  public int getTotalIssueCount() {
    return errors.size() + warnings.size();
  }

  /**
   * Check if there are any errors.
   */
  public boolean hasErrors() {
    return !errors.isEmpty();
  }
}