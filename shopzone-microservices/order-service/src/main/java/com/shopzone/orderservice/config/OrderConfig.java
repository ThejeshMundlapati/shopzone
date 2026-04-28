package com.shopzone.orderservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Data @Configuration @ConfigurationProperties(prefix = "shopzone.order")
public class OrderConfig {
    private BigDecimal taxRate = new BigDecimal("0.08");
    private BigDecimal freeShippingThreshold = new BigDecimal("50.00");
    private BigDecimal flatShippingRate = new BigDecimal("5.99");
    private int cancellationWindowHours = 24;
    private int lowStockThreshold = 10;
}
