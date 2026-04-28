package com.shopzone.orderservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component @Slf4j
public class CartClient {
    private final RestTemplate restTemplate;
    private final String cartUrl;
    public CartClient(RestTemplate restTemplate, @Value("${services.cart-url}") String url) {
        this.restTemplate = restTemplate; this.cartUrl = url;
    }

    public void clearCart(String userId) {
        try {
            restTemplate.delete(cartUrl + "/api/internal/cart/" + userId + "/clear");
        } catch (Exception e) { log.warn("Failed to clear cart: {}", e.getMessage()); }
    }
}
