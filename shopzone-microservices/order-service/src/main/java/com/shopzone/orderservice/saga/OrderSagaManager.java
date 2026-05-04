package com.shopzone.orderservice.saga;

import com.shopzone.common.event.*;
import com.shopzone.orderservice.kafka.OrderEventProducer;
import com.shopzone.orderservice.model.Order;
import com.shopzone.orderservice.model.enums.OrderStatus;
import com.shopzone.orderservice.model.enums.PaymentStatus;
import com.shopzone.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

/**
 * Orchestrates the Order Creation Saga using choreography.
 *
 * Happy path:
 *   STARTED → STOCK_RESERVED → PAYMENT_CREATED → PAYMENT_RECEIVED → COMPLETED
 *
 * Failure paths:
 *   STARTED → STOCK_RESERVE_FAILED → FAILED (nothing to compensate)
 *   STOCK_RESERVED → PAYMENT_FAILED → COMPENSATING → FAILED (restore stock)
 *
 * Each method is called by OrderEventConsumer when the corresponding event arrives.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaManager {

    private final SagaStateRepository sagaStateRepository;
    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    /**
     * Start a new saga when an order is created.
     * Called by CheckoutService after persisting the order.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startSaga(Order order) {
        if (sagaStateRepository.existsByOrderNumber(order.getOrderNumber())) {
            log.warn("Saga already exists for order {}, skipping", order.getOrderNumber());
            return;
        }

        SagaState saga = SagaState.builder()
                .orderNumber(order.getOrderNumber())
                .orderId(order.getId())
                .status(SagaState.STARTED)
                .build();
        sagaStateRepository.save(saga);

        // Publish ORDER_CREATED → Product Service reserves stock
        orderEventProducer.publishOrderCreated(order);
        log.info("Saga STARTED for order {}", order.getOrderNumber());
    }

    /**
     * Stock was successfully reserved by Product Service.
     * Next step: wait for payment (payment is initiated by the frontend/Stripe).
     */
    @Transactional
    public void onStockReserved(StockEvent event) {
        SagaState saga = findSaga(event.getOrderNumber());
        if (saga == null) return;

        // Idempotency: only advance if we're in the right state
        if (!SagaState.STARTED.equals(saga.getStatus())) {
            log.warn("Saga for {} is in state {}, expected STARTED. Skipping STOCK_RESERVED.",
                    event.getOrderNumber(), saga.getStatus());
            return;
        }

        saga.setStatus(SagaState.STOCK_RESERVED);
        saga.setLastEventId(event.getEventId());
        sagaStateRepository.save(saga);

        log.info("Saga STOCK_RESERVED for order {}", event.getOrderNumber());
    }

    /**
     * Stock reservation failed (insufficient stock).
     * Cancel the order — nothing else to compensate since payment hasn't started.
     */
    @Transactional
    public void onStockReserveFailed(StockEvent event) {
        SagaState saga = findSaga(event.getOrderNumber());
        if (saga == null) return;

        if (!SagaState.STARTED.equals(saga.getStatus())) {
            log.warn("Saga for {} is in state {}, expected STARTED. Skipping STOCK_RESERVE_FAILED.",
                    event.getOrderNumber(), saga.getStatus());
            return;
        }

        // Cancel the order
        Order order = orderRepository.findByOrderNumberWithItems(event.getOrderNumber()).orElse(null);
        if (order != null) {
            String reason = String.format("Insufficient stock for %s (requested: %d, available: %d)",
                    event.getFailedProductName(), event.getRequestedQuantity(), event.getAvailableQuantity());
            order.setStatus(OrderStatus.CANCELLED);
            order.setPaymentStatus(PaymentStatus.CANCELLED);
            order.setCancelledBy("SYSTEM");
            order.setCancellationReason(reason);
            order.updateStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            orderEventProducer.publishOrderCancelled(order, reason);
        }

        saga.setStatus(SagaState.FAILED);
        saga.setFailureReason("Stock reservation failed: " + event.getFailedProductName());
        saga.setLastEventId(event.getEventId());
        sagaStateRepository.save(saga);

        log.info("Saga FAILED (stock) for order {}", event.getOrderNumber());
    }

    /**
     * Payment intent was created by Payment Service.
     * Now we wait for the customer to complete payment (via Stripe Elements on frontend).
     */
    @Transactional
    public void onPaymentCreated(PaymentEvent event) {
        SagaState saga = findSaga(event.getOrderNumber());
        if (saga == null) return;

        if (!SagaState.STOCK_RESERVED.equals(saga.getStatus())) {
            log.warn("Saga for {} is in state {}, expected STOCK_RESERVED. Skipping PAYMENT_CREATED.",
                    event.getOrderNumber(), saga.getStatus());
            return;
        }

        // Update order with payment intent ID
        Order order = orderRepository.findByOrderNumberWithItems(event.getOrderNumber()).orElse(null);
        if (order != null) {
            order.setStripePaymentIntentId(event.getStripePaymentIntentId());
            order.setPaymentStatus(PaymentStatus.AWAITING_PAYMENT);
            orderRepository.save(order);
        }

        saga.setStatus(SagaState.PAYMENT_CREATED);
        saga.setLastEventId(event.getEventId());
        sagaStateRepository.save(saga);

        log.info("Saga PAYMENT_CREATED for order {}", event.getOrderNumber());
    }

    /**
     * Payment succeeded (Stripe webhook → Payment Service → Kafka).
     * Confirm the order and complete the saga.
     */
    @Transactional
    public void onPaymentSuccess(PaymentEvent event) {
        SagaState saga = findSaga(event.getOrderNumber());
        if (saga == null) return;

        // Accept payment success from PAYMENT_CREATED or STOCK_RESERVED state
        // (payment can succeed before we process the PAYMENT_CREATED event)
        if (!SagaState.PAYMENT_CREATED.equals(saga.getStatus()) &&
                !SagaState.STOCK_RESERVED.equals(saga.getStatus())) {
            log.warn("Saga for {} is in state {}, expected PAYMENT_CREATED or STOCK_RESERVED. Skipping PAYMENT_SUCCESS.",
                    event.getOrderNumber(), saga.getStatus());
            return;
        }

        Order order = orderRepository.findByOrderNumberWithItems(event.getOrderNumber()).orElse(null);
        if (order != null) {
            order.recordPayment(event.getStripeChargeId(), event.getReceiptUrl());
            orderRepository.save(order);
            orderEventProducer.publishOrderConfirmed(order);
        }

        saga.setStatus(SagaState.COMPLETED);
        saga.setLastEventId(event.getEventId());
        sagaStateRepository.save(saga);

        log.info("Saga COMPLETED for order {}", event.getOrderNumber());
    }

    /**
     * Payment failed.
     * Compensate: restore stock, cancel order.
     */
    @Transactional
    public void onPaymentFailed(PaymentEvent event) {
        SagaState saga = findSaga(event.getOrderNumber());
        if (saga == null) return;

        if (!SagaState.PAYMENT_CREATED.equals(saga.getStatus()) &&
                !SagaState.STOCK_RESERVED.equals(saga.getStatus())) {
            log.warn("Saga for {} is in state {}, skipping PAYMENT_FAILED.", event.getOrderNumber(), saga.getStatus());
            return;
        }

        // Compensate: cancel order and publish cancellation (Product Service will restore stock)
        Order order = orderRepository.findByOrderNumberWithItems(event.getOrderNumber()).orElse(null);
        if (order != null) {
            String reason = "Payment failed: " +
                    (event.getFailureMessage() != null ? event.getFailureMessage() : "unknown error");
            order.setStatus(OrderStatus.CANCELLED);
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setCancelledBy("SYSTEM");
            order.setCancellationReason(reason);
            order.updateStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            // ORDER_CANCELLED event → Product Service restores stock
            orderEventProducer.publishOrderCancelled(order, reason);
        }

        saga.setStatus(SagaState.FAILED);
        saga.setFailureReason("Payment failed: " + event.getFailureMessage());
        saga.setLastEventId(event.getEventId());
        sagaStateRepository.save(saga);

        log.info("Saga FAILED (payment) for order {}. Compensation triggered.", event.getOrderNumber());
    }

    // === Helper ===

    private SagaState findSaga(String orderNumber) {
        return sagaStateRepository.findByOrderNumber(orderNumber).orElseGet(() -> {
            log.warn("No saga found for order {}. Event ignored.", orderNumber);
            return null;
        });
    }
}
