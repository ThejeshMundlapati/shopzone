package com.shopzone.apigateway.config;

/*
╔══════════════════════════════════════════════════════════════════╗                                           ║
║  WHY THIS EXISTS:                                                ║
║  The parent pom includes spring-boot-starter-security globally.  ║
║  By default, Spring Security enables CSRF protection, which      ║
║  blocks all POST/PUT/DELETE requests with 403 Forbidden.         ║
║                                                                  ║
║  The gateway does NOT need Spring Security for auth — our        ║
║  JwtAuthenticationFilter (a GlobalFilter) handles that.          ║
║  So we disable CSRF and permit all requests at the security      ║
║  level, letting our custom filter do the real work.              ║
║                                                                  ║
║  NOTE: This is a REACTIVE security config (WebFlux), not a       ║
║  servlet one, because the gateway runs on Netty.                 ║
╚══════════════════════════════════════════════════════════════════╝
*/

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http
        // Disable CSRF — the gateway is stateless, JWT handles auth
        .csrf(csrf -> csrf.disable())
        // Permit all requests — our JwtAuthenticationFilter handles authorization
        .authorizeExchange(exchanges -> exchanges
            .anyExchange().permitAll()
        )
        // Disable default login form
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable());

    return http.build();
  }
}