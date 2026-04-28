package com.shopzone.cartservice.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shopzone.cartservice.model.Cart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Slf4j
public class CartRepository {
    private static final String KEY_PREFIX = "cart:";
    private static final Duration EXPIRATION = Duration.ofDays(30);
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public Optional<Cart> findByUserId(String userId) {
        try {
            Object val = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
            if (val == null) return Optional.empty();
            if (val instanceof Cart c) return Optional.of(c);
            // Handle Jackson default typing format
            Cart cart = redisTemplate.getValueSerializer() != null
                ? mapper.convertValue(val, Cart.class)
                : mapper.readValue(val.toString(), Cart.class);
            return Optional.of(cart);
        } catch (Exception e) {
            log.error("Error finding cart for {}: {}", userId, e.getMessage());
            // If deserialization fails, delete corrupt data and return empty
            try { redisTemplate.delete(KEY_PREFIX + userId); } catch (Exception ignored) {}
            return Optional.empty();
        }
    }

    public Cart save(Cart cart) {
        if (cart.getCreatedAt() == null) cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        redisTemplate.opsForValue().set(KEY_PREFIX + cart.getUserId(), cart, EXPIRATION);
        return cart;
    }

    public Cart getOrCreateCart(String userId) {
        return findByUserId(userId).orElseGet(() -> save(Cart.builder().userId(userId)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()));
    }
}
