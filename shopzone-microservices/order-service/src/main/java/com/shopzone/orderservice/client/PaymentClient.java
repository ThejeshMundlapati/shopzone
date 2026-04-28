package com.shopzone.orderservice.client;

import com.shopzone.common.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component @Slf4j
public class PaymentClient {
    private final RestTemplate restTemplate;
    private final String paymentUrl;
    public PaymentClient(RestTemplate restTemplate, @Value("${services.payment-url}") String url) {
        this.restTemplate = restTemplate; this.paymentUrl = url;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createPaymentIntent(String orderId, String orderNumber,
            String userId, String userEmail, java.math.BigDecimal amount) {
        try {
            Map<String, Object> body = Map.of("orderId", orderId, "orderNumber", orderNumber,
                "userId", userId, "userEmail", userEmail, "amount", amount);
            ResponseEntity<ApiResponse<Map<String, Object>>> resp = restTemplate.exchange(
                paymentUrl + "/api/internal/payments/create-intent", HttpMethod.POST,
                new HttpEntity<>(body), new ParameterizedTypeReference<>() {});
            if (resp.getBody() != null && resp.getBody().isSuccess()) return resp.getBody().getData();
            return null;
        } catch (Exception e) { log.error("Payment intent creation failed: {}", e.getMessage()); return null; }
    }
}
