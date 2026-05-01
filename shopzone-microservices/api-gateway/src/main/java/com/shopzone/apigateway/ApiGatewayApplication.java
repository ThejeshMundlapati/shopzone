package com.shopzone.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway — Single entry point for ALL client requests.
 *
 * WHY THIS EXISTS:
 * Without a gateway, the frontend needs to know the URL of every service:
 *   - http://localhost:8081/api/auth/login     (user-service)
 *   - http://localhost:8082/api/products       (product-service)
 *   - http://localhost:8083/api/cart           (cart-service)
 *   - etc.
 *
 * With the gateway, the frontend talks to ONE URL:
 *   - http://localhost:8080/api/auth/login     → routed to user-service
 *   - http://localhost:8080/api/products       → routed to product-service
 *   - http://localhost:8080/api/cart           → routed to cart-service
 *
 * WHAT IT DOES:
 * 1. Routes requests to the correct service based on URL path
 * 2. Validates JWT tokens (so individual services don't have to)
 * 3. Rate limits requests (prevent abuse)
 * 4. Logs all incoming requests
 * 5. Handles CORS (so individual services don't have to)
 * 6. Discovers services via Eureka (no hardcoded URLs)
 *
 * PORT: 8080 (same port the monolith used — frontend doesn't need to change)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApiGatewayApplication.class, args);
  }
}