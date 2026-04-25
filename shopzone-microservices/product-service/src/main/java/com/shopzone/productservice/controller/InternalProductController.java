package com.shopzone.productservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.common.dto.response.ProductResponse;
import com.shopzone.common.exception.ResourceNotFoundException;
import com.shopzone.productservice.model.Product;
import com.shopzone.productservice.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Internal endpoints for inter-service calls.
 * Cart Service, Order Service, Review Service call these.
 */
@RestController @RequestMapping("/api/internal/products") @RequiredArgsConstructor @Hidden
public class InternalProductController {
    private final ProductRepository productRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable String id) {
        Product p = productRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return ResponseEntity.ok(ApiResponse.success("Found", toResponse(p)));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByIds(@RequestBody List<String> ids) {
        List<ProductResponse> products = productRepository.findByIdIn(ids).stream()
            .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Found", products));
    }

    @PostMapping("/{id}/reduce-stock")
    public ResponseEntity<ApiResponse<Boolean>> reduceStock(
            @PathVariable String id, @RequestParam int quantity) {
        int result = productRepository.reduceStock(id, quantity, -quantity);
        return ResponseEntity.ok(ApiResponse.success("Stock updated", result > 0));
    }

    @PostMapping("/{id}/increase-stock")
    public ResponseEntity<ApiResponse<Boolean>> increaseStock(
            @PathVariable String id, @RequestParam int quantity) {
        int result = productRepository.increaseStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success("Stock restored", result > 0));
    }

    @PostMapping("/{id}/update-rating")
    public ResponseEntity<ApiResponse<Void>> updateRating(
            @PathVariable String id, @RequestBody Map<String, Object> body) {
        productRepository.findById(id).ifPresent(p -> {
            p.setAverageRating(((Number) body.getOrDefault("averageRating", 0.0)).doubleValue());
            p.setReviewCount(((Number) body.getOrDefault("reviewCount", 0)).intValue());
            productRepository.save(p);
        });
        return ResponseEntity.ok(ApiResponse.success("Rating updated"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Stats", Map.of(
            "total", productRepository.count(),
            "active", productRepository.countByActiveTrue(),
            "outOfStock", productRepository.countOutOfStock(),
            "lowStock", productRepository.countLowStock(10)
        )));
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
            .id(p.getId()).name(p.getName()).description(p.getDescription()).slug(p.getSlug())
            .sku(p.getSku()).price(p.getPrice()).discountPrice(p.getDiscountPrice())
            .discountPercentage(p.getDiscountPercentage()).stock(p.getStock())
            .inStock(p.getStock() != null && p.getStock() > 0)
            .categoryId(p.getCategoryId()).brand(p.getBrand())
            .images(p.getImages()).tags(p.getTags()).active(p.isActive()).featured(p.isFeatured())
            .averageRating(p.getAverageRating()).reviewCount(p.getReviewCount())
            .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
            .build();
    }
}
