package com.shopzone.apigateway.controller;

/*
╔══════════════════════════════════════════════════════════════════╗                                            ║
║                                                                  ║
║  WHY: When a circuit breaker trips (too many failures), requests ║
║  are redirected here instead of hanging. This gives the client   ║
║  a fast, clean error response.                                   ║
║                                                                  ║
║  NOTE: Spring Cloud Gateway is reactive, so we use @RestController║
║  with reactive types (Mono). This is the ONE exception where     ║
║  a controller exists in the gateway.                             ║
╚══════════════════════════════════════════════════════════════════╝
*/

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

  @GetMapping("/user-service")
  public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "success", false,
            "message", "User Service is temporarily unavailable. Please try again later.",
            "service", "user-service"
        )));
  }

  @GetMapping("/product-service")
  public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback() {
    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "success", false,
            "message", "Product Service is temporarily unavailable. Please try again later.",
            "service", "product-service"
        )));
  }

  @GetMapping("/cart-service")
  public Mono<ResponseEntity<Map<String, Object>>> cartServiceFallback() {
    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "success", false,
            "message", "Cart Service is temporarily unavailable. Please try again later.",
            "service", "cart-service"
        )));
  }

  @GetMapping("/order-service")
  public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback() {
    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "success", false,
            "message", "Order Service is temporarily unavailable. Please try again later.",
            "service", "order-service"
        )));
  }

  @GetMapping("/payment-service")
  public Mono<ResponseEntity<Map<String, Object>>> paymentServiceFallback() {
    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "success", false,
            "message", "Payment Service is temporarily unavailable. Please try again later.",
            "service", "payment-service"
        )));
  }

  @GetMapping("/search-service")
  public Mono<ResponseEntity<Map<String, Object>>> searchServiceFallback() {
    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "success", false,
            "message", "Search Service is temporarily unavailable. Please try again later.",
            "service", "search-service"
        )));
  }
}