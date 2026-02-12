package com.shopzone.controller;

import com.shopzone.service.StripeWebhookService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Stripe webhook events.
 * This endpoint receives events from Stripe when payment status changes.
 *
 * IMPORTANT: This endpoint must be publicly accessible (no auth required)
 * and must receive the raw request body for signature verification.
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
@Hidden
public class StripeWebhookController {

  private final StripeWebhookService webhookService;

  /**
   * Handle Stripe webhook events.
   *
   * Stripe sends events here when:
   * - Payment succeeds (payment_intent.succeeded)
   * - Payment fails (payment_intent.payment_failed)
   * - Refund is processed (charge.refunded)
   * - And many other events
   *
   * @param payload Raw request body (required for signature verification)
   * @param sigHeader Stripe-Signature header
   */
  @PostMapping("/stripe")
  public ResponseEntity<String> handleStripeWebhook(
      @RequestBody String payload,
      @RequestHeader("Stripe-Signature") String sigHeader) {

    log.debug("Received Stripe webhook");

    try {
      webhookService.handleWebhook(payload, sigHeader);
      return ResponseEntity.ok("Webhook processed");
    } catch (Exception e) {
      log.error("Webhook processing failed: {}", e.getMessage());
      return ResponseEntity.ok("Webhook received (error logged)");
    }
  }
}