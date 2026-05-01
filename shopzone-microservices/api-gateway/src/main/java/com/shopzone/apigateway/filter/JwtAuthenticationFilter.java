package com.shopzone.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getURI().getPath();
    String method = request.getMethod().name();

    log.debug("Gateway filter -- {} {}", method, path);

    // Skip authentication for public routes
    if (isPublicPath(path, method)) {
      return chain.filter(exchange);
    }

    // Extract Authorization header
    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.warn("Missing or invalid Authorization header for {} {}", method, path);
      return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
    }

    String token = authHeader.substring(7);

    try {
      // Validate token and extract claims
      Claims claims = extractClaims(token);

      String userId = claims.getSubject();
      String email = claims.get("email", String.class);
      String role = claims.get("role", String.class);

      log.debug("JWT valid -- userId={}, email={}, role={}", userId, email, role);

      // Add user info as headers for downstream services
      ServerHttpRequest modifiedRequest = request.mutate()
          .header("X-User-Id", userId)
          .header("X-User-Email", email != null ? email : "")
          .header("X-User-Role", role != null ? role : "CUSTOMER")
          .build();

      return chain.filter(exchange.mutate().request(modifiedRequest).build());

    } catch (Exception e) {
      log.warn("JWT validation failed: {}", e.getMessage());
      return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
    }
  }

  /**
   * Check if the request path is public (no auth needed).
   */
  private boolean isPublicPath(String path, String method) {
    // Auth endpoints — always public
    if (path.startsWith("/api/auth/") || path.equals("/api/auth")) {
      return true;
    }

    // Webhooks — always public (Stripe calls these)
    if (path.startsWith("/api/webhooks")) {
      return true;
    }

    // Actuator — always public
    if (path.startsWith("/actuator")) {
      return true;
    }

    // Swagger/API docs — always public
    if (path.contains("/v3/api-docs") || path.contains("/swagger-ui")) {
      return true;
    }

    // Products, categories, reviews, search — GET only is public
    if ("GET".equalsIgnoreCase(method)) {
      if (path.startsWith("/api/products")) return true;
      if (path.startsWith("/api/categories")) return true;
      if (path.startsWith("/api/reviews")) return true;
      if (path.startsWith("/api/search")) return true;
    }

    // Everything else requires authentication
    return false;
  }

  private Claims extractClaims(String token) {
    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(status);
    response.getHeaders().add("Content-Type", "application/json");
    String body = String.format("{\"success\":false,\"message\":\"%s\"}", message);
    return response.writeWith(
        Mono.just(response.bufferFactory().wrap(body.getBytes()))
    );
  }

  @Override
  public int getOrder() {
    return -1;
  }
}