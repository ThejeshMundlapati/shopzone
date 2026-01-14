package com.shopzone.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shopzone.model.Wishlist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WishlistRepository {

  private static final String WISHLIST_KEY_PREFIX = "wishlist:";

  private final RedisTemplate<String, Object> redisTemplate;

  private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

  private String getWishlistKey(String userId) {
    return WISHLIST_KEY_PREFIX + userId;
  }

  public Optional<Wishlist> findByUserId(String userId) {
    try {
      String key = getWishlistKey(userId);
      Object value = redisTemplate.opsForValue().get(key);

      if (value == null) {
        return Optional.empty();
      }

      if (value instanceof Wishlist wishlist) {
        return Optional.of(wishlist);
      } else if (value instanceof Map) {
        try {
          Wishlist wishlist = mapper.convertValue(value, Wishlist.class);
          return Optional.of(wishlist);
        } catch (IllegalArgumentException e) {
          log.error("Failed to convert Map to Wishlist for user {}", userId, e);
        }
      }

      return Optional.empty();
    } catch (Exception e) {
      log.error("Error finding wishlist for user {}: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  public Wishlist save(Wishlist wishlist) {
    try {
      String key = getWishlistKey(wishlist.getUserId());
      if (wishlist.getCreatedAt() == null) {
        wishlist.setCreatedAt(LocalDateTime.now());
      }
      wishlist.setUpdatedAt(LocalDateTime.now());
      redisTemplate.opsForValue().set(key, wishlist);
      log.debug("Wishlist saved for user: {}", wishlist.getUserId());
      return wishlist;
    } catch (Exception e) {
      log.error("Error saving wishlist for user {}: {}", wishlist.getUserId(), e.getMessage());
      throw new RuntimeException("Failed to save wishlist", e);
    }
  }

  public void deleteByUserId(String userId) {
    try {
      String key = getWishlistKey(userId);
      redisTemplate.delete(key);
      log.debug("Wishlist deleted for user: {}", userId);
    } catch (Exception e) {
      log.error("Error deleting wishlist for user {}: {}", userId, e.getMessage());
    }
  }

  public boolean existsByUserId(String userId) {
    String key = getWishlistKey(userId);
    Boolean exists = redisTemplate.hasKey(key);
    return exists != null && exists;
  }

  public Wishlist getOrCreateWishlist(String userId) {
    return findByUserId(userId).orElseGet(() -> {
      Wishlist newWishlist = Wishlist.builder()
          .userId(userId)
          .createdAt(LocalDateTime.now())
          .updatedAt(LocalDateTime.now())
          .build();
      return save(newWishlist);
    });
  }
}