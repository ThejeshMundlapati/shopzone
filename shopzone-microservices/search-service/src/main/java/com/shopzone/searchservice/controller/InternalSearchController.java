package com.shopzone.searchservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.searchservice.model.ProductDocument;
import com.shopzone.searchservice.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/internal/search") @RequiredArgsConstructor @Slf4j @Hidden
public class InternalSearchController {
    private final ProductSearchService searchService;

    @PostMapping("/sync/{productId}")
    public ResponseEntity<ApiResponse<Void>> syncProduct(@PathVariable String productId, @RequestBody(required = false) ProductDocument doc) {
        if (doc != null) searchService.syncProduct(doc);
        return ResponseEntity.ok(ApiResponse.success("Synced"));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeProduct(@PathVariable String productId) {
        searchService.removeProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Removed"));
    }

    @PostMapping("/update-rating/{productId}")
    public ResponseEntity<ApiResponse<Void>> updateRating(@PathVariable String productId, @RequestBody Map<String, Object> body) {
        searchService.updateRating(productId,
            ((Number) body.getOrDefault("averageRating", 0.0)).doubleValue(),
            ((Number) body.getOrDefault("reviewCount", 0)).intValue());
        return ResponseEntity.ok(ApiResponse.success("Rating updated"));
    }
}
