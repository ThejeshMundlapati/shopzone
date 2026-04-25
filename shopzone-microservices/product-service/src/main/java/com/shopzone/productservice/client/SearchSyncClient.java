package com.shopzone.productservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

/**
 * Calls Search Service to sync product data to Elasticsearch.
 * In Week 17 (Kafka), this becomes an event publish.
 */
@Component @Slf4j
public class SearchSyncClient {
    private final RestTemplate restTemplate;
    private final String searchUrl;

    public SearchSyncClient(RestTemplate restTemplate, @Value("${services.search-url}") String searchUrl) {
        this.restTemplate = restTemplate;
        this.searchUrl = searchUrl;
    }

    @Async
    public void syncProduct(String productId) {
        try {
            restTemplate.postForEntity(searchUrl + "/api/internal/search/sync/" + productId, null, Void.class);
        } catch (Exception e) { log.warn("Search sync failed for product {}: {}", productId, e.getMessage()); }
    }

    @Async
    public void removeProduct(String productId) {
        try {
            restTemplate.delete(searchUrl + "/api/internal/search/remove/" + productId);
        } catch (Exception e) { log.warn("Search remove failed for product {}: {}", productId, e.getMessage()); }
    }

    @Async
    public void updateRating(String productId, Double rating, Integer count) {
        try {
            restTemplate.postForEntity(searchUrl + "/api/internal/search/update-rating/" + productId,
                Map.of("averageRating", rating != null ? rating : 0.0, "reviewCount", count != null ? count : 0), Void.class);
        } catch (Exception e) { log.warn("Rating sync failed: {}", e.getMessage()); }
    }
}
