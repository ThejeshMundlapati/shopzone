package com.shopzone.service;

import com.shopzone.config.StripeConfig;
import com.shopzone.exception.BadRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Low-level service for Stripe API operations.
 * Handles direct communication with Stripe.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

  private final StripeConfig stripeConfig;

  /**
   * Create a Payment Intent for an order.
   *
   * @param amountInDollars Order total in dollars
   * @param orderId Internal order ID
   * @param orderNumber Human-readable order number
   * @param customerEmail Customer's email for receipt
   * @param description Description for the charge
   * @return Created PaymentIntent
   */
  public PaymentIntent createPaymentIntent(
      BigDecimal amountInDollars,
      String orderId,
      String orderNumber,
      String customerEmail,
      String description
  ) {
    try {
      long amountInCents = amountInDollars
          .multiply(BigDecimal.valueOf(100))
          .longValue();

      PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
          .setAmount(amountInCents)
          .setCurrency(stripeConfig.getCurrency())
          .setDescription(description)
          .setReceiptEmail(customerEmail)
          .setStatementDescriptorSuffix("SHOPZONE")
          .setAutomaticPaymentMethods(
              PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                  .setEnabled(true)
                  .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                  .build()
          )
          .putMetadata("order_id", orderId)
          .putMetadata("order_number", orderNumber)
          .putMetadata("source", "shopzone")
          .build();

      PaymentIntent intent = PaymentIntent.create(params);
      log.info("Created PaymentIntent {} for order {}", intent.getId(), orderNumber);

      return intent;

    } catch (StripeException e) {
      log.error("Failed to create PaymentIntent for order {}: {}", orderNumber, e.getMessage());
      throw new BadRequestException("Failed to initialize payment: " + e.getMessage());
    }
  }

  /**
   * Retrieve a Payment Intent by ID.
   */
  public PaymentIntent retrievePaymentIntent(String paymentIntentId) {
    try {
      return PaymentIntent.retrieve(paymentIntentId);
    } catch (StripeException e) {
      log.error("Failed to retrieve PaymentIntent {}: {}", paymentIntentId, e.getMessage());
      throw new BadRequestException("Failed to retrieve payment: " + e.getMessage());
    }
  }

  /**
   * Cancel a Payment Intent.
   */
  public PaymentIntent cancelPaymentIntent(String paymentIntentId) {
    try {
      PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
      return intent.cancel();
    } catch (StripeException e) {
      log.error("Failed to cancel PaymentIntent {}: {}", paymentIntentId, e.getMessage());
      throw new BadRequestException("Failed to cancel payment: " + e.getMessage());
    }
  }

  /**
   * Create a full refund for a payment.
   */
  public Refund createFullRefund(String paymentIntentId, String reason) {
    try {
      RefundCreateParams params = RefundCreateParams.builder()
          .setPaymentIntent(paymentIntentId)
          .setReason(mapRefundReason(reason))
          .putMetadata("reason_text", reason)
          .build();

      Refund refund = Refund.create(params);
      log.info("Created full refund {} for PaymentIntent {}", refund.getId(), paymentIntentId);

      return refund;

    } catch (StripeException e) {
      log.error("Failed to create refund for {}: {}", paymentIntentId, e.getMessage());
      throw new BadRequestException("Failed to process refund: " + e.getMessage());
    }
  }

  /**
   * Create a partial refund for a payment.
   */
  public Refund createPartialRefund(String paymentIntentId, BigDecimal amountInDollars, String reason) {
    try {
      long amountInCents = amountInDollars
          .multiply(BigDecimal.valueOf(100))
          .longValue();

      RefundCreateParams params = RefundCreateParams.builder()
          .setPaymentIntent(paymentIntentId)
          .setAmount(amountInCents)
          .setReason(mapRefundReason(reason))
          .putMetadata("reason_text", reason)
          .build();

      Refund refund = Refund.create(params);
      log.info("Created partial refund {} ({}) for PaymentIntent {}",
          refund.getId(), amountInDollars, paymentIntentId);

      return refund;

    } catch (StripeException e) {
      log.error("Failed to create partial refund for {}: {}", paymentIntentId, e.getMessage());
      throw new BadRequestException("Failed to process refund: " + e.getMessage());
    }
  }

  /**
   * Retrieve a Charge by ID.
   */
  public Charge retrieveCharge(String chargeId) {
    try {
      return Charge.retrieve(chargeId);
    } catch (StripeException e) {
      log.error("Failed to retrieve Charge {}: {}", chargeId, e.getMessage());
      throw new BadRequestException("Failed to retrieve charge: " + e.getMessage());
    }
  }

  /**
   * Extract order ID from PaymentIntent metadata.
   */
  public String getOrderIdFromPaymentIntent(PaymentIntent intent) {
    Map<String, String> metadata = intent.getMetadata();
    return metadata != null ? metadata.get("order_id") : null;
  }

  /**
   * Extract order number from PaymentIntent metadata.
   */
  public String getOrderNumberFromPaymentIntent(PaymentIntent intent) {
    Map<String, String> metadata = intent.getMetadata();
    return metadata != null ? metadata.get("order_number") : null;
  }

  /**
   * Get card details from a Charge.
   */
  public CardDetails getCardDetails(Charge charge) {
    if (charge.getPaymentMethodDetails() != null &&
        charge.getPaymentMethodDetails().getCard() != null) {

      var card = charge.getPaymentMethodDetails().getCard();
      return new CardDetails(
          card.getLast4(),
          card.getBrand()
      );
    }
    return null;
  }

  /**
   * Map reason text to Stripe RefundReason enum.
   */
  private RefundCreateParams.Reason mapRefundReason(String reason) {
    if (reason == null) {
      return RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER;
    }

    String lowerReason = reason.toLowerCase();
    if (lowerReason.contains("duplicate")) {
      return RefundCreateParams.Reason.DUPLICATE;
    } else if (lowerReason.contains("fraud")) {
      return RefundCreateParams.Reason.FRAUDULENT;
    }
    return RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER;
  }

  /**
   * Simple record for card details.
   */
  public record CardDetails(String lastFour, String brand) {}
}