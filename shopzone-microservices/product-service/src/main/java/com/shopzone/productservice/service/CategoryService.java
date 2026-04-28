package com.shopzone.productservice.service;

import com.github.slugify.Slugify;
import com.shopzone.productservice.dto.request.CategoryRequest;
import com.shopzone.productservice.dto.response.BreadcrumbItem;
import com.shopzone.productservice.dto.response.CategoryResponse;
import com.shopzone.productservice.model.Category;
import com.shopzone.productservice.repository.CategoryRepository;
import com.shopzone.productservice.repository.ProductRepository;
import com.shopzone.common.exception.BadRequestException;
import com.shopzone.common.exception.ResourceNotFoundException;
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
        if (categoryRepository.existsByNameIgnoreCase(request.getName()))
            throw new BadRequestException("Category '" + request.getName() + "' already exists");

        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) slug = slugify.slugify(request.getName());
        slug = ensureUniqueSlug(slug, null);

        if (request.getParentId() != null && !request.getParentId().isBlank())
            categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));

        Category category = Category.builder()
                .name(request.getName()).description(request.getDescription()).slug(slug)
                .imageUrl(request.getImageUrl()).parentId(request.getParentId())
                .active(request.isActive()).displayOrder(request.getDisplayOrder())
                .build();
        return buildCategoryResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::buildCategoryResponse).collect(Collectors.toList());
    }

    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIdIsNullAndActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::buildCategoryResponse).collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(String id) {
        return buildCategoryResponse(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id)));
    }

    public CategoryResponse getCategoryBySlug(String slug) {
        return buildCategoryResponse(categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + slug)));
    }

    public List<CategoryResponse> getChildCategories(String parentId) {
        return categoryRepository.findByParentIdAndActiveTrueOrderByDisplayOrderAsc(parentId).stream()
                .map(this::buildCategoryResponse).collect(Collectors.toList());
    }

    public List<BreadcrumbItem> getBreadcrumb(String categoryId) {
        List<BreadcrumbItem> breadcrumb = new ArrayList<>();
        String currentId = categoryId;
        int level = 0;
        while (currentId != null) {
            String id = currentId;
            Category cat = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
            breadcrumb.add(BreadcrumbItem.builder().id(cat.getId()).name(cat.getName())
                    .slug(cat.getSlug()).level(level++).build());
            currentId = cat.getParentId();
        }
        Collections.reverse(breadcrumb);
        for (int i = 0; i < breadcrumb.size(); i++) breadcrumb.get(i).setLevel(i);
        return breadcrumb;
    }

    public List<CategoryResponse> getCategoryTree() {
        return categoryRepository.findByParentIdIsNullAndActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::buildCategoryWithChildren).collect(Collectors.toList());
    }

    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        categoryRepository.findByNameIgnoreCase(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id))
                throw new BadRequestException("Category '" + request.getName() + "' already exists");
        });
        String newSlug = request.getSlug();
        if (newSlug == null || newSlug.isBlank()) newSlug = slugify.slugify(request.getName());
        newSlug = ensureUniqueSlug(newSlug, id);
        if (request.getParentId() != null && !request.getParentId().isBlank()) {
            if (request.getParentId().equals(id)) throw new BadRequestException("Category cannot be its own parent");
            categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
        }
        category.setName(request.getName()); category.setDescription(request.getDescription());
        category.setSlug(newSlug); category.setImageUrl(request.getImageUrl());
        category.setParentId(request.getParentId()); category.setActive(request.isActive());
        category.setDisplayOrder(request.getDisplayOrder());
        return buildCategoryResponse(categoryRepository.save(category));
    }

    public void deleteCategory(String id) {
        categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        long children = categoryRepository.countByParentId(id);
        if (children > 0) throw new BadRequestException("Cannot delete: has " + children + " children");
        long products = productRepository.countByCategoryId(id);
        if (products > 0) throw new BadRequestException("Cannot delete: has " + products + " products");
        categoryRepository.deleteById(id);
    }

    private String ensureUniqueSlug(String slug, String excludeId) {
        String base = slug; int counter = 1;
        while (true) {
            var existing = categoryRepository.findBySlug(slug);
            if (existing.isEmpty() || (excludeId != null && existing.get().getId().equals(excludeId))) break;
            slug = base + "-" + counter++;
        }
        return slug;
    }

    private CategoryResponse buildCategoryResponse(Category c) {
        String parentName = c.getParentId() != null
                ? categoryRepository.findById(c.getParentId()).map(Category::getName).orElse(null) : null;
        int productCount = (int) productRepository.countByCategoryId(c.getId());
        return CategoryResponse.builder().id(c.getId()).name(c.getName()).description(c.getDescription())
                .slug(c.getSlug()).imageUrl(c.getImageUrl()).parentId(c.getParentId()).parentName(parentName)
                .active(c.isActive()).displayOrder(c.getDisplayOrder()).productCount(productCount)
                .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt()).build();
    }

    private CategoryResponse buildCategoryWithChildren(Category c) {
        CategoryResponse response = buildCategoryResponse(c);
        List<Category> children = categoryRepository.findByParentIdAndActiveTrueOrderByDisplayOrderAsc(c.getId());
        if (!children.isEmpty())
            response.setChildren(children.stream().map(this::buildCategoryWithChildren).collect(Collectors.toList()));
        return response;
    }
}
