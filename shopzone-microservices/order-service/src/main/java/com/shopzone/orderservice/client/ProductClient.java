package com.shopzone.orderservice.client;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.common.dto.response.ProductResponse;
import com.shopzone.common.exception.ServiceCommunicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Component @Slf4j
public class ProductClient {
    private final RestTemplate restTemplate;
    private final String productUrl;
    public ProductClient(RestTemplate restTemplate, @Value("${services.product-url}") String url) {
        this.restTemplate = restTemplate; this.productUrl = url;
    }

    public ProductResponse getProduct(String id) {
        try {
            ResponseEntity<ApiResponse<ProductResponse>> resp = restTemplate.exchange(
                productUrl + "/api/internal/products/" + id, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            if (resp.getBody() != null && resp.getBody().isSuccess()) return resp.getBody().getData();
            throw new ServiceCommunicationException("product-service", "Product not found: " + id);
        } catch (ServiceCommunicationException e) { throw e; }
        catch (Exception e) { throw new ServiceCommunicationException("product-service", e.getMessage(), e); }
    }

    public List<ProductResponse> getProductsByIds(List<String> ids) {
        try {
            HttpEntity<List<String>> entity = new HttpEntity<>(ids);
            ResponseEntity<ApiResponse<List<ProductResponse>>> resp = restTemplate.exchange(
                productUrl + "/api/internal/products/batch", HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});
            if (resp.getBody() != null && resp.getBody().isSuccess()) return resp.getBody().getData();
            return List.of();
        } catch (Exception e) { log.error("Failed to fetch products: {}", e.getMessage()); return List.of(); }
    }

    public boolean reduceStock(String productId, int quantity) {
        try {
            restTemplate.postForEntity(productUrl + "/api/internal/products/" + productId +
                "/reduce-stock?quantity=" + quantity, null, Void.class);
            return true;
        } catch (Exception e) { log.error("Stock reduction failed: {}", e.getMessage()); return false; }
    }

    public void increaseStock(String productId, int quantity) {
        try {
            restTemplate.postForEntity(productUrl + "/api/internal/products/" + productId +
                "/increase-stock?quantity=" + quantity, null, Void.class);
        } catch (Exception e) { log.error("Stock restore failed: {}", e.getMessage()); }
    }
}
