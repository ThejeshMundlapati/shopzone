//package com.shopzone.security;
//
//import com.shopzone.config.JwtConfig;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.UUID;
//
//@Component
//public class JwtTokenProvider {
//
//  private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
//
//  private final JwtConfig jwtConfig;
//  private SecretKey secretKey;
//
//  public JwtTokenProvider(JwtConfig jwtConfig) {
//    this.jwtConfig = jwtConfig;
//  }
//
//  @PostConstruct
//  public void init() {
//    byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
//    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
//  }
//
//  public String generateToken(Authentication authentication) {
//    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//    return generateToken(userDetails.getUsername());
//  }
//
//  public String generateToken(String username) {
//    Date now = new Date();
//    Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());
//
//    return Jwts.builder()
//        .subject(username)
//        .issuedAt(now)
//        .expiration(expiryDate)
//        .id(UUID.randomUUID().toString())
//        .signWith(secretKey, Jwts.SIG.HS256)
//        .compact();
//  }
//
//  public String generateRefreshToken(String username) {
//    Date now = new Date();
//    Date expiryDate = new Date(now.getTime() + jwtConfig.getRefreshExpiration());
//
//    return Jwts.builder()
//        .subject(username)
//        .issuedAt(now)
//        .expiration(expiryDate)
//        .id(UUID.randomUUID().toString())
//        .claim("type", "refresh")
//        .signWith(secretKey, Jwts.SIG.HS256)
//        .compact();
//  }
//
//  public String getUsernameFromToken(String token) {
//    return getClaims(token).getSubject();
//  }
//
//  public Date getExpirationFromToken(String token) {
//    return getClaims(token).getExpiration();
//  }
//
//  public boolean validateToken(String token) {
//    try {
//      Jwts.parser()
//          .verifyWith(secretKey)
//          .build()
//          .parseSignedClaims(token);
//      return true;
//    } catch (MalformedJwtException e) {
//      log.error("Invalid JWT token: {}", e.getMessage());
//    } catch (ExpiredJwtException e) {
//      log.error("JWT token is expired: {}", e.getMessage());
//    } catch (UnsupportedJwtException e) {
//      log.error("JWT token is unsupported: {}", e.getMessage());
//    } catch (IllegalArgumentException e) {
//      log.error("JWT claims string is empty: {}", e.getMessage());
//    } catch (SecurityException e) {
//      log.error("JWT signature validation failed: {}", e.getMessage());
//    }
//    return false;
//  }
//
//  public boolean isTokenExpired(String token) {
//    try {
//      Date expiration = getExpirationFromToken(token);
//      return expiration.before(new Date());
//    } catch (ExpiredJwtException e) {
//      return true;
//    }
//  }
//
//  public boolean isRefreshToken(String token) {
//    try {
//      Claims claims = getClaims(token);
//      String type = claims.get("type", String.class);
//      return "refresh".equals(type);
//    } catch (Exception e) {
//      return false;
//    }
//  }
//
//  public long getTokenRemainingTime(String token) {
//    Date expiration = getExpirationFromToken(token);
//    return expiration.getTime() - System.currentTimeMillis();
//  }
//
//  private Claims getClaims(String token) {
//    return Jwts.parser()
//        .verifyWith(secretKey)
//        .build()
//        .parseSignedClaims(token)
//        .getPayload();
//  }
//}