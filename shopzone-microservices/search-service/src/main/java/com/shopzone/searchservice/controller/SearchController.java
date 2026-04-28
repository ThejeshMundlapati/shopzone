package com.shopzone.searchservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.searchservice.service.ProductSearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/search") @RequiredArgsConstructor
@Tag(name = "Search", description = "Product search")
public class SearchController {
    private final ProductSearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(
            @RequestParam(required = false) String q, @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice, @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brand, @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean inStock, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size, @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success("Search completed",
            searchService.search(q, minPrice, maxPrice, categoryId, brand, minRating, inStock, page, size, sortBy, sortDir)));
    }
}
