package com.shopzone.paymentservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Map;

@Component @Slf4j
public class OrderClient {
    private final RestTemplate restTemplate;
    private final String orderUrl;
    public OrderClient(RestTemplate restTemplate, @Value("${services.order-url}") String url) {
        this.restTemplate = restTemplate; this.orderUrl = url;
    }

    public void recordPayment(String orderId, String chargeId, String receiptUrl) {
        try { restTemplate.postForEntity(orderUrl + "/api/internal/orders/" + orderId + "/record-payment",
            Map.of("chargeId", chargeId != null ? chargeId : "", "receiptUrl", receiptUrl != null ? receiptUrl : ""), Void.class); }
        catch (Exception e) { log.error("Failed to record payment on order: {}", e.getMessage()); }
    }

    public void recordPaymentFailure(String orderId) {
        try { restTemplate.postForEntity(orderUrl + "/api/internal/orders/" + orderId + "/record-payment-failure", null, Void.class); }
        catch (Exception e) { log.error("Failed to record failure on order: {}", e.getMessage()); }
    }

    public void recordRefund(String orderId, BigDecimal amount, boolean restoreStock) {
        try { restTemplate.postForEntity(orderUrl + "/api/internal/orders/" + orderId + "/record-refund",
            Map.of("amount", amount, "restoreStock", restoreStock), Void.class); }
        catch (Exception e) { log.error("Failed to record refund on order: {}", e.getMessage()); }
    }
}
