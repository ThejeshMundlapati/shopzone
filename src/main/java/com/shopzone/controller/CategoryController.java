package com.shopzone.controller;

import com.shopzone.dto.request.CategoryRequest;
import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.response.BreadcrumbItem;
import com.shopzone.dto.response.CategoryResponse;
import com.shopzone.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {

  private final CategoryService categoryService;


  @GetMapping
  @Operation(summary = "Get all categories", description = "Returns all active categories")
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
    List<CategoryResponse> categories = categoryService.getAllCategories();
    return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
  }

  @GetMapping("/roots")
  @Operation(summary = "Get root categories", description = "Returns only top-level categories (no parent)")
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {
    List<CategoryResponse> categories = categoryService.getRootCategories();
    return ResponseEntity.ok(ApiResponse.success("Root categories retrieved successfully", categories));
  }

  @GetMapping("/tree")
  @Operation(summary = "Get category tree", description = "Returns hierarchical category structure")
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
    List<CategoryResponse> tree = categoryService.getCategoryTree();
    return ResponseEntity.ok(ApiResponse.success("Category tree retrieved successfully", tree));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get category by ID", description = "Returns a category by its ID")
  public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable String id) {
    CategoryResponse category = categoryService.getCategoryById(id);
    return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
  }

  @GetMapping("/slug/{slug}")
  @Operation(summary = "Get category by slug", description = "Returns a category by its URL-friendly slug")
  public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(@PathVariable String slug) {
    CategoryResponse category = categoryService.getCategoryBySlug(slug);
    return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
  }

  @GetMapping("/{id}/children")
  @Operation(summary = "Get child categories", description = "Returns direct children of a category")
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> getChildCategories(@PathVariable String id) {
    List<CategoryResponse> children = categoryService.getChildCategories(id);
    return ResponseEntity.ok(ApiResponse.success("Child categories retrieved successfully", children));
  }

  @GetMapping("/{id}/breadcrumb")
  @Operation(summary = "Get category breadcrumb", description = "Returns breadcrumb trail from root to this category")
  public ResponseEntity<ApiResponse<List<BreadcrumbItem>>> getBreadcrumb(@PathVariable String id) {
    List<BreadcrumbItem> breadcrumb = categoryService.getBreadcrumb(id);
    return ResponseEntity.ok(ApiResponse.success("Breadcrumb retrieved successfully", breadcrumb));
  }


  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create category", description = "Create a new category (Admin only)",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
      @Valid @RequestBody CategoryRequest request) {

    log.info("Creating category: {}", request.getName());
    CategoryResponse category = categoryService.createCategory(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Category created successfully", category));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update category", description = "Update an existing category (Admin only)",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
      @PathVariable String id,
      @Valid @RequestBody CategoryRequest request) {

    log.info("Updating category: {}", id);
    CategoryResponse category = categoryService.updateCategory(id, request);
    return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete category", description = "Delete a category (Admin only). Cannot delete if has children or products.",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String id) {
    log.info("Deleting category: {}", id);
    categoryService.deleteCategory(id);
    return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
  }
}