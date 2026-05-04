package com.shopzone.notificationservice.kafka;

import com.shopzone.common.config.KafkaTopicConfig;
import com.shopzone.common.event.NotificationEvent;
import com.shopzone.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final EmailService emailService;

    @KafkaListener(
        topics = KafkaTopicConfig.NOTIFICATION_EVENTS_TOPIC,
        groupId = "notification-service-group",
        containerFactory = "notificationEventListenerFactory"
    )
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event: {} for order {}",
            event.getNotificationType(), event.getOrderNumber());

        try {
            switch (event.getNotificationType()) {
                case "ORDER_CONFIRMED" -> emailService.sendOrderConfirmation(
                    event.getUserEmail(), event.getUserFullName(), event.getOrderNumber());
                case "ORDER_SHIPPED" -> emailService.sendOrderShipped(
                    event.getUserEmail(), event.getUserFullName(), event.getOrderNumber(),
                    event.getTrackingNumber(), event.getShippingCarrier());
                case "ORDER_DELIVERED" -> emailService.sendOrderDelivered(
                    event.getUserEmail(), event.getUserFullName(), event.getOrderNumber());
                case "ORDER_CANCELLED" -> emailService.sendOrderCancelled(
                    event.getUserEmail(), event.getUserFullName(), event.getOrderNumber(),
                    event.getCancellationReason());
                default -> log.warn("Unknown notification type: {}", event.getNotificationType());
            }
            log.info("Email sent for {} — order {}", event.getNotificationType(), event.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send email for {} — order {}: {}",
                event.getNotificationType(), event.getOrderNumber(), e.getMessage(), e);
        }
    }
}