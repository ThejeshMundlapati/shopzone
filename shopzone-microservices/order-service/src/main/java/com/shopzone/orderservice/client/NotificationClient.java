package com.shopzone.orderservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component @Slf4j
public class NotificationClient {
    private final RestTemplate restTemplate;
    private final String notificationUrl;
    public NotificationClient(RestTemplate restTemplate, @Value("${services.notification-url}") String url) {
        this.restTemplate = restTemplate; this.notificationUrl = url;
    }

    @Async
    public void sendOrderConfirmation(String orderNumber, String userEmail, String userName) {
        send("/api/internal/notifications/order-confirmation",
            Map.of("orderNumber", orderNumber, "email", userEmail, "customerName", userName));
    }

    @Async
    public void sendOrderShipped(String orderNumber, String email, String name, String tracking, String carrier) {
        send("/api/internal/notifications/order-shipped",
            Map.of("orderNumber", orderNumber, "email", email, "customerName", name,
                "trackingNumber", tracking != null ? tracking : "", "carrier", carrier != null ? carrier : ""));
    }

    @Async
    public void sendOrderDelivered(String orderNumber, String email, String name) {
        send("/api/internal/notifications/order-delivered",
            Map.of("orderNumber", orderNumber, "email", email, "customerName", name));
    }

    @Async
    public void sendOrderCancelled(String orderNumber, String email, String name, String reason) {
        send("/api/internal/notifications/order-cancelled",
            Map.of("orderNumber", orderNumber, "email", email, "customerName", name, "reason", reason != null ? reason : ""));
    }

    private void send(String path, Map<String, String> body) {
        try { restTemplate.postForEntity(notificationUrl + path, body, Void.class); }
        catch (Exception e) { log.warn("Notification failed: {}", e.getMessage()); }
    }
}
