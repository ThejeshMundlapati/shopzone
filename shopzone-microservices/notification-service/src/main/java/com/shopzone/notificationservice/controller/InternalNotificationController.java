package com.shopzone.notificationservice.controller;

import com.shopzone.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/internal/notifications") @RequiredArgsConstructor @Slf4j
public class InternalNotificationController {
    private final EmailService emailService;

    @PostMapping("/welcome")
    public ResponseEntity<Void> welcome(@RequestBody Map<String, String> body) {
        emailService.sendWelcome(body.get("email"), body.get("firstName"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Void> passwordReset(@RequestBody Map<String, String> body) {
        emailService.sendPasswordReset(body.get("email"), body.get("firstName"), body.get("resetToken"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/order-confirmation")
    public ResponseEntity<Void> orderConfirmation(@RequestBody Map<String, String> body) {
        emailService.sendOrderConfirmation(body.get("email"), body.get("customerName"), body.get("orderNumber"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/order-shipped")
    public ResponseEntity<Void> orderShipped(@RequestBody Map<String, String> body) {
        emailService.sendOrderShipped(body.get("email"), body.get("customerName"),
            body.get("orderNumber"), body.get("trackingNumber"), body.get("carrier"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/order-delivered")
    public ResponseEntity<Void> orderDelivered(@RequestBody Map<String, String> body) {
        emailService.sendOrderDelivered(body.get("email"), body.get("customerName"), body.get("orderNumber"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/order-cancelled")
    public ResponseEntity<Void> orderCancelled(@RequestBody Map<String, String> body) {
        emailService.sendOrderCancelled(body.get("email"), body.get("customerName"),
            body.get("orderNumber"), body.get("reason"));
        return ResponseEntity.ok().build();
    }
}
