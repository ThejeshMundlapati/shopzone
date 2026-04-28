package com.shopzone.paymentservice.service;

import com.shopzone.common.exception.BadRequestException;
import com.shopzone.paymentservice.config.StripeConfig;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service @RequiredArgsConstructor @Slf4j
public class StripeService {
    private final StripeConfig stripeConfig;

    public PaymentIntent createPaymentIntent(BigDecimal amount, String orderId, String orderNumber,
            String email, String description) {
        try {
            long cents = amount.multiply(BigDecimal.valueOf(100)).longValue();
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(cents).setCurrency(stripeConfig.getCurrency())
                .setDescription(description).setReceiptEmail(email)
                .setStatementDescriptorSuffix("SHOPZONE")
                .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true).setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER).build())
                .putMetadata("order_id", orderId).putMetadata("order_number", orderNumber).putMetadata("source", "shopzone")
                .build();
            return PaymentIntent.create(params);
        } catch (StripeException e) { throw new BadRequestException("Payment init failed: " + e.getMessage()); }
    }

    public PaymentIntent retrievePaymentIntent(String id) {
        try { return PaymentIntent.retrieve(id); }
        catch (StripeException e) { throw new BadRequestException("Failed to retrieve payment: " + e.getMessage()); }
    }

    public void cancelPaymentIntent(String id) {
        try { PaymentIntent.retrieve(id).cancel(); }
        catch (StripeException e) { log.warn("Cancel failed: {}", e.getMessage()); }
    }

    public Refund createFullRefund(String intentId, String reason) {
        try { return Refund.create(RefundCreateParams.builder().setPaymentIntent(intentId)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER).build()); }
        catch (StripeException e) { throw new BadRequestException("Refund failed: " + e.getMessage()); }
    }

    public Refund createPartialRefund(String intentId, BigDecimal amount, String reason) {
        try { return Refund.create(RefundCreateParams.builder().setPaymentIntent(intentId)
                .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER).build()); }
        catch (StripeException e) { throw new BadRequestException("Refund failed: " + e.getMessage()); }
    }

    public Charge retrieveCharge(String chargeId) {
        try { return Charge.retrieve(chargeId); }
        catch (StripeException e) { throw new BadRequestException("Charge retrieve failed: " + e.getMessage()); }
    }

    public record CardDetails(String lastFour, String brand) {}
    public CardDetails getCardDetails(Charge charge) {
        if (charge.getPaymentMethodDetails() != null && charge.getPaymentMethodDetails().getCard() != null) {
            var card = charge.getPaymentMethodDetails().getCard();
            return new CardDetails(card.getLast4(), card.getBrand());
        }
        return null;
    }
}
