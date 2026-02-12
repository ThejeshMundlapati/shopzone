package com.shopzone.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe configuration for payment processing.
 * Initializes Stripe SDK with API keys on application startup.
 */
@Configuration
@Getter
@Slf4j
public class StripeConfig {

  @Value("${stripe.secret-key}")
  private String secretKey;

  @Value("${stripe.public-key}")
  private String publicKey;

  @Value("${stripe.webhook-secret}")
  private String webhookSecret;

  @Value("${stripe.currency:usd}")
  private String currency;

  @Value("${stripe.success-url:http://localhost:3000/payment/success}")
  private String successUrl;

  @Value("${stripe.cancel-url:http://localhost:3000/payment/cancel}")
  private String cancelUrl;

  /**
   * Initialize Stripe SDK with the secret key.
   * Called automatically after bean construction.
   */
  @PostConstruct
  public void init() {
    Stripe.apiKey = secretKey;
    log.info("Stripe SDK initialized with API version: {}", Stripe.API_VERSION);

    if (secretKey != null && secretKey.startsWith("sk_test_")) {
      log.info("Stripe running in TEST mode - no real charges will be made");
    } else if (secretKey != null && secretKey.startsWith("sk_live_")) {
      log.warn("⚠️ Stripe running in LIVE mode - real charges will be made!");
    }
  }

  /**
   * Check if Stripe is configured with test keys.
   */
  public boolean isTestMode() {
    return secretKey != null && secretKey.startsWith("sk_test_");
  }

  /**
   * Get the Stripe API version being used.
   */
  public String getApiVersion() {
    return Stripe.API_VERSION;
  }
}