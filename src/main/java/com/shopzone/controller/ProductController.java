package com.shopzone.controller;

import com.shopzone.dto.request.ProductRequest;
import com.shopzone.dto.request.ProductUpdateRequest;
import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.response.PagedResponse;
import com.shopzone.dto.response.ProductResponse;
import com.shopzone.service.CloudinaryService;
import com.shopzone.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

  private final ProductService productService;
  private final CloudinaryService cloudinaryService;


  @GetMapping
  @Operation(summary = "Get all products", description = "Returns paginated list of all active products")
  public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAllProducts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    PagedResponse<ProductResponse> products = productService.getAllProducts(page, size, sortBy, sortDir);
    return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get product by ID", description = "Returns a product by its ID")
  public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String id) {
    ProductResponse product = productService.getProductById(id);
    return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
  }

  @GetMapping("/slug/{slug}")
  @Operation(summary = "Get product by slug", description = "Returns a product by its URL-friendly slug")
  public ResponseEntity<ApiResponse<ProductResponse>> getProductBySlug(@PathVariable String slug) {
    ProductResponse product = productService.getProductBySlug(slug);
    return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
  }

  @GetMapping("/category/{categoryId}")
  @Operation(summary = "Get products by category", description = "Returns products in a specific category")
  public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getProductsByCategory(
      @PathVariable String categoryId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    PagedResponse<ProductResponse> products = productService.getProductsByCategory(categoryId, page, size);
    return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
  }

  @GetMapping("/search")
  @Operation(summary = "Search products", description = "Search products by name, description, brand, or tags")
  public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> searchProducts(
      @RequestParam String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    PagedResponse<ProductResponse> products = productService.searchProducts(query, page, size);
    return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", products));
  }

  @GetMapping("/filter/price")
  @Operation(summary = "Filter by price range", description = "Filter products by minimum and maximum price")
  public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> filterByPrice(
      @RequestParam BigDecimal minPrice,
      @RequestParam BigDecimal maxPrice,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    PagedResponse<ProductResponse> products = productService.filterByPriceRange(minPrice, maxPrice, page, size);
    return ResponseEntity.ok(ApiResponse.success("Products filtered successfully", products));
  }

  @GetMapping("/filter/brand")
  @Operation(summary = "Filter by brand", description = "Filter products by brand name")
  public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> filterByBrand(
      @RequestParam String brand,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    PagedResponse<ProductResponse> products = productService.filterByBrand(brand, page, size);
    return ResponseEntity.ok(ApiResponse.success("Products filtered successfully", products));
  }

  @GetMapping("/featured")
  @Operation(summary = "Get featured products", description = "Returns all featured products")
  public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getFeaturedProducts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    PagedResponse<ProductResponse> products = productService.getFeaturedProducts(page, size);
    return ResponseEntity.ok(ApiResponse.success("Featured products retrieved successfully", products));
  }


  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create product", description = "Create a new product (Admin only)",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
      @Valid @RequestBody ProductRequest request) {

    log.info("Creating product: {}", request.getName());
    ProductResponse product = productService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Product created successfully", product));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update product", description = "Partially update an existing product - only provided fields will be updated (Admin only)",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
      @PathVariable String id,
      @Valid @RequestBody ProductUpdateRequest request) {

    log.info("Updating product: {}", id);
    ProductResponse product = productService.partialUpdateProduct(id, request);
    return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete product", description = "Delete a product (Admin only)",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
    log.info("Deleting product: {}", id);
    productService.deleteProduct(id);
    return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
  }


  @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Upload product images", description = "Upload one or more images for a product (Admin only)",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<ProductResponse>> uploadImages(
      @PathVariable String id,
      @Parameter(description = "Image files to upload (JPEG, PNG, WebP, GIF)")
      @RequestPart("files") MultipartFile[] files) {

    log.info("Uploading {} images for product: {}", files.length, id);

    ProductResponse product = null;
    for (MultipartFile file : files) {
      String imageUrl = cloudinaryService.uploadImage(file);
      product = productService.addImage(id, imageUrl);
    }

    return ResponseEntity.ok(ApiResponse.success("Images uploaded successfully", product));
  }

  @DeleteMapping("/{id}/images")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Remove product image", description = "Remove an image from a product (Admin only)",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<ProductResponse>> removeImage(
      @PathVariable String id,
      @RequestParam String imageUrl) {

    log.info("Removing image from product: {}", id);
    ProductResponse product = productService.removeImage(id, imageUrl);
    return ResponseEntity.ok(ApiResponse.success("Image removed successfully", product));
  }
}