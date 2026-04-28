package com.shopzone.cartservice.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shopzone.cartservice.model.Wishlist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Slf4j
public class WishlistRepository {
    private static final String KEY_PREFIX = "wishlist:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public Optional<Wishlist> findByUserId(String userId) {
        try {
            Object val = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
            if (val == null) return Optional.empty();
            if (val instanceof Wishlist w) return Optional.of(w);
            if (val instanceof Map) return Optional.of(mapper.convertValue(val, Wishlist.class));
            return Optional.empty();
        } catch (Exception e) { return Optional.empty(); }
    }

    public Wishlist save(Wishlist wishlist) {
        if (wishlist.getCreatedAt() == null) wishlist.setCreatedAt(LocalDateTime.now());
        wishlist.setUpdatedAt(LocalDateTime.now());
        redisTemplate.opsForValue().set(KEY_PREFIX + wishlist.getUserId(), wishlist);
        return wishlist;
    }

    public Wishlist getOrCreateWishlist(String userId) {
        return findByUserId(userId).orElseGet(() -> save(Wishlist.builder().userId(userId)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()));
    }
}
