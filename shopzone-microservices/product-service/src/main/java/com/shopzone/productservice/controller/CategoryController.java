package com.shopzone.productservice.controller;

import com.shopzone.productservice.dto.request.CategoryRequest;
import com.shopzone.productservice.dto.response.BreadcrumbItem;
import com.shopzone.productservice.dto.response.CategoryResponse;
import com.shopzone.productservice.service.CategoryService;
import com.shopzone.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/categories") @RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Categories", categoryService.getAllCategories()));
    }
    @GetMapping("/roots")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> roots() {
        return ResponseEntity.ok(ApiResponse.success("Root categories", categoryService.getRootCategories()));
    }
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> tree() {
        return ResponseEntity.ok(ApiResponse.success("Category tree", categoryService.getCategoryTree()));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Category", categoryService.getCategoryById(id)));
    }
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success("Category", categoryService.getCategoryBySlug(slug)));
    }
    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> children(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Children", categoryService.getChildCategories(id)));
    }
    @GetMapping("/{id}/breadcrumb")
    public ResponseEntity<ApiResponse<List<BreadcrumbItem>>> breadcrumb(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Breadcrumb", categoryService.getBreadcrumb(id)));
    }
    @PostMapping @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created", categoryService.createCategory(request)));
    }
    @PutMapping("/{id}") @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable String id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Updated", categoryService.updateCategory(id, request)));
    }
    @DeleteMapping("/{id}") @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        categoryService.deleteCategory(id); return ResponseEntity.ok(ApiResponse.success("Deleted"));
    }
}
