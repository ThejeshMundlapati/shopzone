package com.shopzone.paymentservice.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration @Getter @Slf4j
public class StripeConfig {
    @Value("${stripe.secret-key}") private String secretKey;
    @Value("${stripe.public-key}") private String publicKey;
    @Value("${stripe.webhook-secret}") private String webhookSecret;
    @Value("${stripe.currency:usd}") private String currency;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe initialized (test={})", secretKey != null && secretKey.startsWith("sk_test_"));
    }
}
