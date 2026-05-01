package com.shopzone.common.config;

/*
╔══════════════════════════════════════════════════════════════════╗
║  FILE: shopzone-common/src/main/java/com/shopzone/common/       ║
║        config/RestClientConfig.java                              ║
║  ACTION: MODIFY — add @LoadBalanced annotation                   ║
╚══════════════════════════════════════════════════════════════════╝
*/

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    /**
     * Load-balanced RestTemplate.
     *
     * With @LoadBalanced, URLs like "http://user-service/api/internal/users/..."
     * are resolved via Eureka. Spring replaces "user-service" with the actual
     * host:port (e.g., "192.168.1.5:8081") before making the HTTP call.
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}