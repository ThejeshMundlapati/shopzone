package com.shopzone.common.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate configuration for inter-service communication.
 * All services use this to call other services via HTTP.
 *
 * In Week 16 (API Gateway + Eureka), these URLs become service names
 * resolved by Eureka. For now, they're direct localhost URLs.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
