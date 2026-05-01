package com.shopzone.discoveryserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Eureka Dashboard.
 *
 * WHY: Eureka dashboard should not be publicly accessible.
 * We protect it with basic auth (username/password in application.yml).
 * BUT we disable CSRF for the /eureka/** endpoints because
 * microservices register via POST requests and CSRF would block them.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // Disable CSRF for Eureka client registration endpoints
    http.csrf(csrf -> csrf.ignoringRequestMatchers("/eureka/**"))
        .authorizeHttpRequests(auth -> auth
            // Actuator health endpoint — public (for Docker health checks)
            .requestMatchers("/actuator/health").permitAll()
            // Everything else requires authentication
            .anyRequest().authenticated()
        )
        .httpBasic(httpBasic -> {});

    return http.build();
  }
}