package com.shopzone.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {

  /**
   * Secret key for signing JWT tokens.
   * Should be at least 256 bits (32 characters) for HS256 algorithm.
   */
  private String secret;

  /**
   * Access token expiration time in milliseconds.
   * Default: 24 hours (86400000 ms)
   */
  private Long expiration = 86400000L;

  /**
   * Refresh token expiration time in milliseconds.
   * Default: 7 days (604800000 ms)
   */
  private Long refreshExpiration = 604800000L;

  /**
   * Token prefix used in Authorization header.
   * Default: "Bearer "
   */
  private String tokenPrefix = "Bearer ";

  /**
   * Header name for Authorization.
   * Default: "Authorization"
   */
  private String headerString = "Authorization";
}