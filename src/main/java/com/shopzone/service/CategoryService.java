package com.shopzone.service;

import com.github.slugify.Slugify;
import com.shopzone.dto.request.CategoryRequest;
import com.shopzone.dto.response.BreadcrumbItem;
import com.shopzone.dto.response.CategoryResponse;
import com.shopzone.exception.BadRequestException;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.Category;
import com.shopzone.repository.mongo.CategoryRepository;
import com.shopzone.repository.mongo.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final Slugify slugify = Slugify.builder().build();


  public CategoryResponse createCategory(CategoryRequest request) {
    log.info("Creating category: {}", request.getName());

    if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
      throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
    }

    String slug = request.getSlug();
    if (slug == null || slug.isBlank()) {
      slug = slugify.slugify(request.getName());
    }

    slug = ensureUniqueSlug(slug, null);

    if (request.getParentId() != null && !request.getParentId().isBlank()) {
      categoryRepository.findById(request.getParentId())
          .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
    }

    Category category = Category.builder()
        .name(request.getName())
        .description(request.getDescription())
        .slug(slug)
        .imageUrl(request.getImageUrl())
        .parentId(request.getParentId())
        .active(request.isActive())
        .displayOrder(request.getDisplayOrder())
        .build();

    Category saved = categoryRepository.save(category);
    log.info("Category created with ID: {}", saved.getId());

    return buildCategoryResponse(saved);
  }


  public List<CategoryResponse> getAllCategories() {
    log.info("Fetching all active categories");
    return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc()
        .stream()
        .map(this::buildCategoryResponse)
        .collect(Collectors.toList());
  }

  public List<CategoryResponse> getRootCategories() {
    log.info("Fetching root categories");
    return categoryRepository.findByParentIdIsNullAndActiveTrueOrderByDisplayOrderAsc()
        .stream()
        .map(this::buildCategoryResponse)
        .collect(Collectors.toList());
  }

  public CategoryResponse getCategoryById(String id) {
    log.info("Fetching category by ID: {}", id);
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    return buildCategoryResponse(category);
  }

  public CategoryResponse getCategoryBySlug(String slug) {
    log.info("Fetching category by slug: {}", slug);
    Category category = categoryRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
    return buildCategoryResponse(category);
  }

  public List<CategoryResponse> getChildCategories(String parentId) {
    log.info("Fetching child categories for parent: {}", parentId);
    return categoryRepository.findByParentIdAndActiveTrueOrderByDisplayOrderAsc(parentId)
        .stream()
        .map(this::buildCategoryResponse)
        .collect(Collectors.toList());
  }

  public List<BreadcrumbItem> getBreadcrumb(String categoryId) {
    log.info("Building breadcrumb for category: {}", categoryId);
    List<BreadcrumbItem> breadcrumb = new ArrayList<>();
    String currentId = categoryId;
    int level = 0;

    while (currentId != null) {
      String id = currentId;
      Category category = categoryRepository.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

      breadcrumb.add(BreadcrumbItem.builder()
          .id(category.getId())
          .name(category.getName())
          .slug(category.getSlug())
          .level(level++)
          .build());

      currentId = category.getParentId();
    }

    Collections.reverse(breadcrumb);

    for (int i = 0; i < breadcrumb.size(); i++) {
      breadcrumb.get(i).setLevel(i);
    }

    return breadcrumb;
  }

  public List<CategoryResponse> getCategoryTree() {
    log.info("Building category tree");
    List<Category> rootCategories = categoryRepository
        .findByParentIdIsNullAndActiveTrueOrderByDisplayOrderAsc();

    return rootCategories.stream()
        .map(this::buildCategoryWithChildren)
        .collect(Collectors.toList());
  }


  public CategoryResponse updateCategory(String id, CategoryRequest request) {
    log.info("Updating category: {}", id);

    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

    categoryRepository.findByNameIgnoreCase(request.getName())
        .ifPresent(existing -> {
          if (!existing.getId().equals(id)) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
          }
        });

    String newSlug = request.getSlug();
    if (newSlug == null || newSlug.isBlank()) {
      newSlug = slugify.slugify(request.getName());
    }
    newSlug = ensureUniqueSlug(newSlug, id);

    if (request.getParentId() != null && !request.getParentId().isBlank()) {
      if (request.getParentId().equals(id)) {
        throw new BadRequestException("Category cannot be its own parent");
      }
      categoryRepository.findById(request.getParentId())
          .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
    }

    category.setName(request.getName());
    category.setDescription(request.getDescription());
    category.setSlug(newSlug);
    category.setImageUrl(request.getImageUrl());
    category.setParentId(request.getParentId());
    category.setActive(request.isActive());
    category.setDisplayOrder(request.getDisplayOrder());

    Category updated = categoryRepository.save(category);
    log.info("Category updated: {}", updated.getId());

    return buildCategoryResponse(updated);
  }


  public void deleteCategory(String id) {
    log.info("Deleting category: {}", id);

    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

    long childCount = categoryRepository.countByParentId(id);
    if (childCount > 0) {
      throw new BadRequestException("Cannot delete category with " + childCount + " child categories. Delete children first.");
    }

    long productCount = productRepository.countByCategoryId(id);
    if (productCount > 0) {
      throw new BadRequestException("Cannot delete category with " + productCount + " products. Move or delete products first.");
    }

    categoryRepository.delete(category);
    log.info("Category deleted: {}", id);
  }


  private String ensureUniqueSlug(String slug, String excludeId) {
    String baseSlug = slug;
    int counter = 1;

    while (true) {
      var existing = categoryRepository.findBySlug(slug);
      if (existing.isEmpty() || (excludeId != null && existing.get().getId().equals(excludeId))) {
        break;
      }
      slug = baseSlug + "-" + counter++;
    }

    return slug;
  }

  private CategoryResponse buildCategoryResponse(Category category) {
    String parentName = null;
    if (category.getParentId() != null) {
      parentName = categoryRepository.findById(category.getParentId())
          .map(Category::getName)
          .orElse(null);
    }

    int productCount = (int) productRepository.countByCategoryId(category.getId());

    return CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .description(category.getDescription())
        .slug(category.getSlug())
        .imageUrl(category.getImageUrl())
        .parentId(category.getParentId())
        .parentName(parentName)
        .active(category.isActive())
        .displayOrder(category.getDisplayOrder())
        .productCount(productCount)
        .createdAt(category.getCreatedAt())
        .updatedAt(category.getUpdatedAt())
        .build();
  }

  private CategoryResponse buildCategoryWithChildren(Category category) {
    CategoryResponse response = buildCategoryResponse(category);

    List<Category> children = categoryRepository
        .findByParentIdAndActiveTrueOrderByDisplayOrderAsc(category.getId());

    if (!children.isEmpty()) {
      response.setChildren(children.stream()
          .map(this::buildCategoryWithChildren)
          .collect(Collectors.toList()));
    }

    return response;
  }
}