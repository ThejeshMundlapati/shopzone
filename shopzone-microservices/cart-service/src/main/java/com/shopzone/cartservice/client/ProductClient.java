package com.shopzone.cartservice.client;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.common.dto.response.ProductResponse;
import com.shopzone.common.exception.ServiceCommunicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component @Slf4j
public class ProductClient {
    private final RestTemplate restTemplate;
    private final String productUrl;

    public ProductClient(RestTemplate restTemplate, @Value("${services.product-url}") String productUrl) {
        this.restTemplate = restTemplate;
        this.productUrl = productUrl;
    }

    public ProductResponse getProduct(String productId) {
        try {
            ResponseEntity<ApiResponse<ProductResponse>> response = restTemplate.exchange(
                productUrl + "/api/internal/products/" + productId,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
            if (response.getBody() != null && response.getBody().isSuccess()) return response.getBody().getData();
            throw new ServiceCommunicationException("product-service", "Product not found: " + productId);
        } catch (ServiceCommunicationException e) { throw e; }
        catch (Exception e) { throw new ServiceCommunicationException("product-service", e.getMessage(), e); }
    }
}
