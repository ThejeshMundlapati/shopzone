package com.shopzone.discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Service Discovery Server.
 *
 * WHY THIS EXISTS:
 * In Week 14-15, services used hardcoded URLs like "http://localhost:8081".
 * That breaks if a service moves to a different host/port.
 * Eureka solves this — services register themselves, and other services
 * look them up by NAME (e.g., "user-service") instead of URL.
 *
 * HOW IT WORKS:
 * 1. This server starts on port 8761
 * 2. Each microservice registers itself with Eureka on startup
 * 3. When order-service needs to call user-service, it asks Eureka
 *    "where is user-service?" and Eureka returns the host:port
 * 4. If user-service is running on multiple instances, Eureka
 *    returns ALL of them and the client does load balancing
 *
 * DASHBOARD: http://localhost:8761 (username: eureka, password: password)
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(DiscoveryServerApplication.class, args);
  }
}