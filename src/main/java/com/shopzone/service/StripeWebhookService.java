package com.shopzone.service;

import com.shopzone.config.StripeConfig;
import com.shopzone.exception.BadRequestException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for handling Stripe webhook events.
 * Processes payment events and updates orders accordingly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

  private final StripeConfig stripeConfig;
  private final PaymentService paymentService;
  private final StripeService stripeService;

  /**
   * Process a webhook event from Stripe.
   */
  public void handleWebhook(String payload, String sigHeader) {
    Event event;

    try {
      event = Webhook.constructEvent(
          payload,
          sigHeader,
          stripeConfig.getWebhookSecret()
      );
    } catch (SignatureVerificationException e) {
      log.error("Invalid webhook signature");
      throw new BadRequestException("Invalid webhook signature");
    }

    log.info("Processing webhook event: {} ({})", event.getType(), event.getId());

    switch (event.getType()) {
      case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event);
      case "payment_intent.payment_failed" -> handlePaymentIntentFailed(event);
      case "payment_intent.canceled" -> handlePaymentIntentCanceled(event);
      case "charge.succeeded" -> handleChargeSucceeded(event);
      case "charge.refunded" -> handleChargeRefunded(event);
      default -> log.debug("Unhandled event type: {}", event.getType());
    }
  }

  /**
   * Safely deserialize PaymentIntent from event.
   * Uses deserializeUnsafe() as fallback when API versions mismatch.
   */
  private PaymentIntent deserializePaymentIntent(Event event) {
    EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

    if (deserializer.getObject().isPresent()) {
      return (PaymentIntent) deserializer.getObject().get();
    }

    log.debug("Using unsafe deserialization for event {} (API version mismatch)", event.getId());
    try {
      StripeObject obj = deserializer.deserializeUnsafe();
      if (obj instanceof PaymentIntent) {
        return (PaymentIntent) obj;
      }
      log.warn("Deserialized object is not a PaymentIntent: {}", obj.getClass().getSimpleName());
      return null;
    } catch (Exception e) {
      log.error("Failed to deserialize PaymentIntent from event {}: {}", event.getId(), e.getMessage());
      return null;
    }
  }

  /**
   * Safely deserialize Charge from event.
   */
  private Charge deserializeCharge(Event event) {
    EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

    if (deserializer.getObject().isPresent()) {
      return (Charge) deserializer.getObject().get();
    }

    log.debug("Using unsafe deserialization for Charge event {}", event.getId());
    try {
      StripeObject obj = deserializer.deserializeUnsafe();
      if (obj instanceof Charge) {
        return (Charge) obj;
      }
      log.warn("Deserialized object is not a Charge: {}", obj.getClass().getSimpleName());
      return null;
    } catch (Exception e) {
      log.error("Failed to deserialize Charge from event {}: {}", event.getId(), e.getMessage());
      return null;
    }
  }

  /**
   * Handle successful payment intent.
   */
  private void handlePaymentIntentSucceeded(Event event) {
    PaymentIntent intent = deserializePaymentIntent(event);

    if (intent == null) {
      log.warn("Could not deserialize PaymentIntent from event {} - skipping", event.getId());
      return;
    }

    log.info("PaymentIntent succeeded: {}", intent.getId());

    if (!paymentService.existsByPaymentIntentId(intent.getId())) {
      log.debug("PaymentIntent {} not found in our system - likely a test event", intent.getId());
      return;
    }

    String chargeId = null;
    String receiptUrl = null;
    String cardLast4 = null;
    String cardBrand = null;

    if (intent.getLatestCharge() != null) {
      try {
        Charge charge = stripeService.retrieveCharge(intent.getLatestCharge());
        chargeId = charge.getId();
        receiptUrl = charge.getReceiptUrl();

        StripeService.CardDetails cardDetails = stripeService.getCardDetails(charge);
        if (cardDetails != null) {
          cardLast4 = cardDetails.lastFour();
          cardBrand = cardDetails.brand();
        }
      } catch (Exception e) {
        log.warn("Failed to retrieve charge details: {}", e.getMessage());
      }
    }

    paymentService.handlePaymentSuccess(
        intent.getId(),
        chargeId,
        receiptUrl,
        cardLast4,
        cardBrand
    );

    log.info("Successfully processed payment for PaymentIntent: {}", intent.getId());
  }

  /**
   * Handle failed payment intent.
   */
  private void handlePaymentIntentFailed(Event event) {
    PaymentIntent intent = deserializePaymentIntent(event);

    if (intent == null) {
      log.warn("Could not deserialize PaymentIntent from event {} - skipping", event.getId());
      return;
    }

    log.warn("PaymentIntent failed: {}", intent.getId());

    if (!paymentService.existsByPaymentIntentId(intent.getId())) {
      log.debug("PaymentIntent {} not found in our system", intent.getId());
      return;
    }

    String failureCode = null;
    String failureMessage = null;

    if (intent.getLastPaymentError() != null) {
      failureCode = intent.getLastPaymentError().getCode();
      failureMessage = intent.getLastPaymentError().getMessage();
    }

    paymentService.handlePaymentFailure(
        intent.getId(),
        failureCode,
        failureMessage
    );
  }

  /**
   * Handle canceled payment intent.
   */
  private void handlePaymentIntentCanceled(Event event) {
    PaymentIntent intent = deserializePaymentIntent(event);

    if (intent == null) {
      log.warn("Could not deserialize PaymentIntent from event {} - skipping", event.getId());
      return;
    }

    log.info("PaymentIntent canceled: {}", intent.getId());

    if (!paymentService.existsByPaymentIntentId(intent.getId())) {
      return;
    }

    paymentService.handlePaymentFailure(
        intent.getId(),
        "canceled",
        "Payment was canceled"
    );
  }

  /**
   * Handle successful charge.
   */
  private void handleChargeSucceeded(Event event) {
    Charge charge = deserializeCharge(event);

    if (charge == null) {
      log.warn("Could not deserialize Charge from event {} - skipping", event.getId());
      return;
    }

    log.info("Charge succeeded: {} for PaymentIntent: {}", charge.getId(), charge.getPaymentIntent());
  }

  /**
   * Handle refunded charge.
   */
  private void handleChargeRefunded(Event event) {
    Charge charge = deserializeCharge(event);

    if (charge == null) {
      log.warn("Could not deserialize Charge from event {} - skipping", event.getId());
      return;
    }

    log.info("Charge refunded: {}", charge.getId());
  }
}