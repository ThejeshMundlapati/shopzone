package com.shopzone.paymentservice.kafka;

import com.shopzone.common.config.KafkaTopicConfig;
import com.shopzone.common.event.PaymentEvent;
import com.shopzone.paymentservice.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCreated(Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventType("PAYMENT_CREATED")
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .orderNumber(payment.getOrderNumber())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .paymentId(payment.getId())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .build();

        send(payment.getOrderNumber(), event);
        log.info("Published PAYMENT_CREATED for order {}", payment.getOrderNumber());
    }

    public void publishPaymentSuccess(Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventType("PAYMENT_SUCCESS")
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .orderNumber(payment.getOrderNumber())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .paymentId(payment.getId())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .stripeChargeId(payment.getStripeChargeId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .receiptUrl(payment.getReceiptUrl())
                .cardLastFour(payment.getCardLastFour())
                .cardBrand(payment.getCardBrand())
                .build();

        send(payment.getOrderNumber(), event);
        log.info("Published PAYMENT_SUCCESS for order {}", payment.getOrderNumber());
    }

    public void publishPaymentFailed(Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventType("PAYMENT_FAILED")
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .orderNumber(payment.getOrderNumber())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .paymentId(payment.getId())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .failureCode(payment.getFailureCode())
                .failureMessage(payment.getFailureMessage())
                .build();

        send(payment.getOrderNumber(), event);
        log.info("Published PAYMENT_FAILED for order {}", payment.getOrderNumber());
    }

    private void send(String orderNumber, PaymentEvent event) {
        kafkaTemplate.send(KafkaTopicConfig.PAYMENT_EVENTS_TOPIC, orderNumber, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) log.error("Failed to publish {} for {}: {}",
                            event.getEventType(), orderNumber, ex.getMessage());
                });
    }
}
