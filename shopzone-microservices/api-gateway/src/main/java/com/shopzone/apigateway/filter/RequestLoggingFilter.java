package com.shopzone.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();

    // Generate unique request ID
    String requestId = UUID.randomUUID().toString().substring(0, 8);
    long startTime = System.currentTimeMillis();

    String method = request.getMethod().name();
    String path = request.getURI().getPath();
    String ip = request.getRemoteAddress() != null
        ? request.getRemoteAddress().getAddress().getHostAddress()
        : "unknown";

    // Add request ID header to the REQUEST (for downstream services)
    ServerHttpRequest modifiedRequest = request.mutate()
        .header("X-Request-Id", requestId)
        .build();

    // Add request ID to RESPONSE headers BEFORE the chain executes
    // (response headers become read-only after the response is committed)
    exchange.getResponse().getHeaders().add("X-Request-Id", requestId);

    log.info("[{}] -> {} {} from {}", requestId, method, path, ip);

    return chain.filter(exchange.mutate().request(modifiedRequest).build())
        .then(Mono.fromRunnable(() -> {
          long duration = System.currentTimeMillis() - startTime;
          int statusCode = exchange.getResponse().getStatusCode() != null
              ? exchange.getResponse().getStatusCode().value()
              : 0;

          log.info("[{}] <- {} {} -- {} ({}ms)",
              requestId, method, path, statusCode, duration);
        }));
  }

  @Override
  public int getOrder() {
    return -2;
  }
}