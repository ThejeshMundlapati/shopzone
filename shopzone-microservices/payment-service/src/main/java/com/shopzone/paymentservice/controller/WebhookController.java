package com.shopzone.paymentservice.controller;

import com.shopzone.paymentservice.service.WebhookService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/webhooks") @RequiredArgsConstructor @Slf4j @Hidden
public class WebhookController {
    private final WebhookService webhookService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handle(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sig) {
        try { webhookService.handleWebhook(payload, sig); return ResponseEntity.ok("OK"); }
        catch (Exception e) { log.error("Webhook error: {}", e.getMessage()); return ResponseEntity.ok("Error logged"); }
    }
}
