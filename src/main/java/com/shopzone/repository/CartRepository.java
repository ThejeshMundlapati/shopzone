package com.shopzone.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shopzone.model.Cart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CartRepository {

  private static final String CART_KEY_PREFIX = "cart:";
  private static final Duration CART_EXPIRATION = Duration.ofDays(30);

  private final RedisTemplate<String, Object> redisTemplate;

  private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

  private String getCartKey(String userId) {
    return CART_KEY_PREFIX + userId;
  }

  public Optional<Cart> findByUserId(String userId) {
    try {
      String key = getCartKey(userId);
      Object value = redisTemplate.opsForValue().get(key);

      if (value == null) {
        return Optional.empty();
      }

      if (value instanceof Cart cart) {
        return Optional.of(cart);
      } else if (value instanceof Map) {
        try {
          Cart cart = mapper.convertValue(value, Cart.class);
          return Optional.of(cart);
        } catch (IllegalArgumentException e) {
          log.error("Failed to convert Map to Cart for user {}", userId, e);
        }
      }

      return Optional.empty();
    } catch (Exception e) {
      log.error("Error finding cart for user {}: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  public Cart save(Cart cart) {
    try {
      String key = getCartKey(cart.getUserId());
      if (cart.getCreatedAt() == null) {
        cart.setCreatedAt(LocalDateTime.now());
      }
      cart.setUpdatedAt(LocalDateTime.now());
      redisTemplate.opsForValue().set(key, cart, CART_EXPIRATION);
      log.debug("Cart saved for user: {}", cart.getUserId());
      return cart;
    } catch (Exception e) {
      log.error("Error saving cart for user {}: {}", cart.getUserId(), e.getMessage());
      throw new RuntimeException("Failed to save cart", e);
    }
  }

  public void deleteByUserId(String userId) {
    try {
      String key = getCartKey(userId);
      redisTemplate.delete(key);
      log.debug("Cart deleted for user: {}", userId);
    } catch (Exception e) {
      log.error("Error deleting cart for user {}: {}", userId, e.getMessage());
    }
  }

  public boolean existsByUserId(String userId) {
    String key = getCartKey(userId);
    Boolean exists = redisTemplate.hasKey(key);
    return exists != null && exists;
  }

  public Cart getOrCreateCart(String userId) {
    return findByUserId(userId).orElseGet(() -> {
      Cart newCart = Cart.builder()
          .userId(userId)
          .createdAt(LocalDateTime.now())
          .updatedAt(LocalDateTime.now())
          .build();
      return save(newCart);
    });
  }

  public void refreshExpiration(String userId) {
    String key = getCartKey(userId);
    redisTemplate.expire(key, CART_EXPIRATION);
  }
}