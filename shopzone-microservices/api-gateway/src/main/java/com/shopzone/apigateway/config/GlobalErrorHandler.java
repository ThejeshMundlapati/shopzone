package com.shopzone.apigateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
@Order(-1)
@Slf4j
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    String path = exchange.getRequest().getURI().getPath();

    HttpStatus status;
    String message;

    if (ex instanceof ConnectException) {
      // Service is down
      status = HttpStatus.SERVICE_UNAVAILABLE;
      message = "Service temporarily unavailable. Please try again later.";
      log.error("Service unreachable for path {}: {}", path, ex.getMessage());
    } else if (ex instanceof TimeoutException) {
      // Service took too long
      status = HttpStatus.GATEWAY_TIMEOUT;
      message = "Request timed out. Please try again.";
      log.error("Timeout for path {}: {}", path, ex.getMessage());
    } else if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
      status = HttpStatus.SERVICE_UNAVAILABLE;
      message = "Service temporarily unavailable. Please try again later.";
      log.error("Connection refused for path {}: {}", path, ex.getMessage());
    } else {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      message = "An unexpected error occurred.";
      log.error("Unexpected error for path {}: ", path, ex);
    }

    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> errorBody = Map.of(
        "success", false,
        "message", message,
        "status", status.value(),
        "path", path
    );

    byte[] bytes;
    try {
      bytes = objectMapper.writeValueAsBytes(errorBody);
    } catch (JsonProcessingException e) {
      bytes = "{\"success\":false,\"message\":\"Internal error\"}".getBytes();
    }

    return exchange.getResponse().writeWith(
        Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
    );
  }
}