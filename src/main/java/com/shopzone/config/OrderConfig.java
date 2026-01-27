package com.shopzone.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Configuration properties for order management.
 *
 * Values are loaded from application.yml under 'shopzone.order' prefix.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "shopzone.order")
public class OrderConfig {

  /**
   * Tax rate as decimal (e.g., 0.08 for 8%).
   * Default: 8%
   */
  private BigDecimal taxRate = new BigDecimal("0.08");

  /**
   * Minimum order amount for free shipping.
   * Default: $50.00
   */
  private BigDecimal freeShippingThreshold = new BigDecimal("50.00");

  /**
   * Flat shipping rate when order is below free shipping threshold.
   * Default: $5.99
   */
  private BigDecimal flatShippingRate = new BigDecimal("5.99");

  /**
   * Number of hours after order placement during which cancellation is allowed.
   * Default: 24 hours
   */
  private int cancellationWindowHours = 24;

  /**
   * Low stock alert threshold.
   * Default: 10 units
   */
  private int lowStockThreshold = 10;
}