package com.shopzone.productservice.controller;

import com.shopzone.productservice.dto.request.ProductRequest;
import com.shopzone.productservice.dto.request.ProductUpdateRequest;
import com.shopzone.productservice.service.CloudinaryService;
import com.shopzone.productservice.service.ProductService;
import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.common.dto.response.PagedResponse;
import com.shopzone.common.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Slf4j @RestController @RequestMapping("/api/products") @RequiredArgsConstructor
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {
    private final ProductService productService;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", productService.getAllProducts(page, size, sortBy, sortDir)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Product retrieved", productService.getProductById(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success("Product retrieved", productService.getProductBySlug(slug)));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> byCategory(
            @PathVariable String categoryId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", productService.getProductsByCategory(categoryId, page, size)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> search(
            @RequestParam String query, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success("Search results", productService.searchProducts(query, page, size)));
    }

    @GetMapping("/filter/price")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> filterPrice(
            @RequestParam BigDecimal minPrice, @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success("Filtered", productService.filterByPriceRange(minPrice, maxPrice, page, size)));
    }

    @GetMapping("/filter/brand")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> filterBrand(
            @RequestParam String brand, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success("Filtered", productService.filterByBrand(brand, page, size)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> featured(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success("Featured", productService.getFeaturedProducts(page, size)));
    }

    @PostMapping
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created", productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable String id, @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Updated", productService.partialUpdateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        productService.deleteProduct(id); return ResponseEntity.ok(ApiResponse.success("Deleted"));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProductResponse>> uploadImages(@PathVariable String id, @RequestPart("files") MultipartFile[] files) {
        ProductResponse product = null;
        for (MultipartFile file : files) { String url = cloudinaryService.uploadImage(file); product = productService.addImage(id, url); }
        return ResponseEntity.ok(ApiResponse.success("Images uploaded", product));
    }

    @DeleteMapping("/{id}/images")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProductResponse>> removeImage(@PathVariable String id, @RequestParam String imageUrl) {
        return ResponseEntity.ok(ApiResponse.success("Image removed", productService.removeImage(id, imageUrl)));
    }
}
