package com.shopzone.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

  /**
   * Determines the rate limiting key for each request.
   *
   * If the request has an X-User-Id header (set by JWT filter after auth),
   * use that as the key — so each user gets their own rate limit bucket.
   *
   * If no user ID (anonymous request), use the IP address.
   * This prevents a single IP from flooding the API.
   */
  @Bean
  public KeyResolver userKeyResolver() {
    return exchange -> {
      String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
      if (userId != null && !userId.isEmpty()) {
        return Mono.just("user:" + userId);
      }
      // Fall back to IP address for anonymous requests
      String ip = exchange.getRequest()
          .getRemoteAddress() != null
          ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
          : "unknown";
      return Mono.just("ip:" + ip);
    };
  }
}