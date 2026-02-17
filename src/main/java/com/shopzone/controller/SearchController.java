package com.shopzone.controller;

import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.request.ProductSearchRequest;
import com.shopzone.dto.response.AutocompleteResponse;
import com.shopzone.dto.response.SearchResultResponse;
import com.shopzone.model.elasticsearch.ProductDocument;
import com.shopzone.service.ProductSearchService;
import com.shopzone.service.ProductSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Product search with Elasticsearch")
public class SearchController {

  private final ProductSearchService productSearchService;
  private final ProductSyncService productSyncService;


  @GetMapping
  @Operation(summary = "Search products with filters and sorting")
  public ResponseEntity<ApiResponse<SearchResultResponse>> search(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Double minPrice,
      @RequestParam(required = false) Double maxPrice,
      @RequestParam(required = false) String categoryId,
      @RequestParam(required = false) String brand,
      @RequestParam(required = false) List<String> tags,
      @RequestParam(required = false) Double minRating,
      @RequestParam(required = false) Boolean inStock,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "12") Integer size,
      @RequestParam(defaultValue = "relevance") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    ProductSearchRequest request = ProductSearchRequest.builder()
        .query(q)
        .minPrice(minPrice)
        .maxPrice(maxPrice)
        .categoryId(categoryId)
        .brand(brand)
        .tags(tags)
        .minRating(minRating)
        .inStock(inStock)
        .page(page)
        .size(Math.min(size, 100))
        .sortBy(sortBy)
        .sortDir(sortDir)
        .build();

    SearchResultResponse results = productSearchService.search(request);
    return ResponseEntity.ok(ApiResponse.success("Search completed", results));
  }

  @PostMapping
  @Operation(summary = "Search products (POST with body)")
  public ResponseEntity<ApiResponse<SearchResultResponse>> searchPost(
      @RequestBody ProductSearchRequest request) {

    if (request.getSize() > 100) {
      request.setSize(100);
    }

    SearchResultResponse results = productSearchService.search(request);
    return ResponseEntity.ok(ApiResponse.success("Search completed", results));
  }

  @GetMapping("/autocomplete")
  @Operation(summary = "Get autocomplete suggestions")
  public ResponseEntity<ApiResponse<AutocompleteResponse>> autocomplete(
      @RequestParam String q,
      @RequestParam(defaultValue = "10") Integer limit) {

    AutocompleteResponse suggestions = productSearchService.autocomplete(q, Math.min(limit, 20));
    return ResponseEntity.ok(ApiResponse.success("Suggestions retrieved", suggestions));
  }

  @GetMapping("/similar/{productId}")
  @Operation(summary = "Get similar products")
  public ResponseEntity<ApiResponse<List<ProductDocument>>> getSimilarProducts(
      @PathVariable String productId,
      @RequestParam(defaultValue = "6") Integer limit) {

    List<ProductDocument> similar = productSearchService.findSimilarProducts(
        productId,
        Math.min(limit, 20)
    );
    return ResponseEntity.ok(ApiResponse.success("Similar products retrieved", similar));
  }


  @PostMapping("/admin/sync")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Admin: Trigger full sync to Elasticsearch",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<String>> triggerFullSync() {
    productSyncService.syncAllProducts();
    return ResponseEntity.ok(ApiResponse.success("Full sync started", "Sync initiated in background"));
  }

  @GetMapping("/admin/sync/status")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Admin: Get sync status",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<ProductSyncService.SyncStatus>> getSyncStatus() {
    ProductSyncService.SyncStatus status = productSyncService.getSyncStatus();
    return ResponseEntity.ok(ApiResponse.success("Sync status retrieved", status));
  }

  @PostMapping("/admin/sync/product/{productId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Admin: Sync single product",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<String>> syncProduct(@PathVariable String productId) {
    productSyncService.syncProductById(productId);
    return ResponseEntity.ok(ApiResponse.success("Product synced", "Product " + productId + " synced"));
  }

  @PostMapping("/admin/sync/category/{categoryId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Admin: Reindex all products in a category",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<String>> reindexCategory(@PathVariable String categoryId) {
    productSyncService.reindexCategory(categoryId);
    return ResponseEntity.ok(ApiResponse.success("Category reindexed",
        "Category " + categoryId + " products reindexed"));
  }
}