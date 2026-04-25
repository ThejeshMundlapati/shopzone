package com.shopzone.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * REST client for calling Notification Service.
 * All calls are async — we don't block user operations for email.
 * In Week 17 (Kafka), these become event publishes instead.
 */
@Component
@Slf4j
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final String notificationUrl;

    public NotificationClient(RestTemplate restTemplate,
                              @Value("${services.notification-url}") String notificationUrl) {
        this.restTemplate = restTemplate;
        this.notificationUrl = notificationUrl;
    }

    @Async
    public void sendWelcomeEmail(String email, String firstName) {
        try {
            restTemplate.postForEntity(
                    notificationUrl + "/api/internal/notifications/welcome",
                    Map.of("email", email, "firstName", firstName),
                    Void.class
            );
            log.info("Welcome email request sent for: {}", email);
        } catch (Exception e) {
            log.warn("Failed to send welcome email request: {}", e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String email, String firstName, String resetToken) {
        try {
            restTemplate.postForEntity(
                    notificationUrl + "/api/internal/notifications/password-reset",
                    Map.of("email", email, "firstName", firstName, "resetToken", resetToken),
                    Void.class
            );
            log.info("Password reset email request sent for: {}", email);
        } catch (Exception e) {
            log.warn("Failed to send password reset email request: {}", e.getMessage());
        }
    }
}
