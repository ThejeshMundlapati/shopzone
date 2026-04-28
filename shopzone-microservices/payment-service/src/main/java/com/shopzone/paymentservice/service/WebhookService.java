package com.shopzone.paymentservice.service;

import com.shopzone.common.exception.BadRequestException;
import com.shopzone.paymentservice.config.StripeConfig;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor @Slf4j
public class WebhookService {
    private final StripeConfig stripeConfig;
    private final PaymentService paymentService;
    private final StripeService stripeService;

    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try { event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret()); }
        catch (SignatureVerificationException e) { throw new BadRequestException("Invalid signature"); }

        log.info("Webhook: {} ({})", event.getType(), event.getId());
        switch (event.getType()) {
            case "payment_intent.succeeded" -> handleSuccess(event);
            case "payment_intent.payment_failed" -> handleFailed(event);
            default -> log.debug("Unhandled: {}", event.getType());
        }
    }

    private void handleSuccess(Event event) {
        PaymentIntent intent = deserializePI(event);
        if (intent == null || !paymentService.existsByPaymentIntentId(intent.getId())) return;

        String chargeId = null, receiptUrl = null, last4 = null, brand = null;
        if (intent.getLatestCharge() != null) {
            try {
                Charge charge = stripeService.retrieveCharge(intent.getLatestCharge());
                chargeId = charge.getId(); receiptUrl = charge.getReceiptUrl();
                StripeService.CardDetails cd = stripeService.getCardDetails(charge);
                if (cd != null) { last4 = cd.lastFour(); brand = cd.brand(); }
            } catch (Exception e) { log.warn("Charge details failed: {}", e.getMessage()); }
        }
        paymentService.handlePaymentSuccess(intent.getId(), chargeId, receiptUrl, last4, brand);
    }

    private void handleFailed(Event event) {
        PaymentIntent intent = deserializePI(event);
        if (intent == null || !paymentService.existsByPaymentIntentId(intent.getId())) return;
        String code = null, msg = null;
        if (intent.getLastPaymentError() != null) {
            code = intent.getLastPaymentError().getCode();
            msg = intent.getLastPaymentError().getMessage();
        }
        paymentService.handlePaymentFailure(intent.getId(), code, msg);
    }

    private PaymentIntent deserializePI(Event event) {
        EventDataObjectDeserializer d = event.getDataObjectDeserializer();
        if (d.getObject().isPresent()) return (PaymentIntent) d.getObject().get();
        try { StripeObject obj = d.deserializeUnsafe(); return obj instanceof PaymentIntent pi ? pi : null; }
        catch (Exception e) { log.error("Deserialize failed: {}", e.getMessage()); return null; }
    }
}
