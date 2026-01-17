package com.shopzone.service;

import com.github.slugify.Slugify;
import com.shopzone.dto.request.ProductRequest;
import com.shopzone.dto.request.ProductUpdateRequest;
import com.shopzone.dto.response.PagedResponse;
import com.shopzone.dto.response.ProductResponse;
import com.shopzone.exception.BadRequestException;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.Category;
import com.shopzone.model.Product;
import com.shopzone.repository.mongo.CategoryRepository;
import com.shopzone.repository.mongo.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final CloudinaryService cloudinaryService;
  private final Slugify slugify = Slugify.builder().build();


  public ProductResponse createProduct(ProductRequest request) {
    log.info("Creating product: {}", request.getName());

    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    String slug = request.getSlug();
    if (slug == null || slug.isBlank()) {
      slug = slugify.slugify(request.getName());
    }
    slug = ensureUniqueSlug(slug, null);

    if (request.getSku() != null && !request.getSku().isBlank()) {
      if (productRepository.existsBySku(request.getSku())) {
        throw new BadRequestException("Product with SKU '" + request.getSku() + "' already exists");
      }
    }

    Integer discountPercentage = calculateDiscountPercentage(request.getPrice(), request.getDiscountPrice());

    Product product = Product.builder()
        .name(request.getName())
        .description(request.getDescription())
        .slug(slug)
        .sku(request.getSku())
        .price(request.getPrice())
        .discountPrice(request.getDiscountPrice())
        .discountPercentage(discountPercentage)
        .stock(request.getStock() != null ? request.getStock() : 0)
        .categoryId(request.getCategoryId())
        .brand(request.getBrand())
        .images(new ArrayList<>())
        .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
        .active(request.isActive())
        .featured(request.isFeatured())
        .details(mapProductDetails(request.getDetails()))
        .build();

    Product saved = productRepository.save(product);
    log.info("Product created with ID: {}", saved.getId());

    return buildProductResponse(saved, category.getName());
  }


  public PagedResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
    log.info("Fetching all products - page: {}, size: {}", page, size);

    Sort sort = sortDir.equalsIgnoreCase("desc")
        ? Sort.by(sortBy).descending()
        : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<Product> productPage = productRepository.findByActiveTrue(pageable);
    return buildPagedResponse(productPage);
  }

  public ProductResponse getProductById(String id) {
    log.info("Fetching product by ID: {}", id);
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    return buildProductResponse(product, getCategoryName(product.getCategoryId()));
  }

  public ProductResponse getProductBySlug(String slug) {
    log.info("Fetching product by slug: {}", slug);
    Product product = productRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
    return buildProductResponse(product, getCategoryName(product.getCategoryId()));
  }

  public PagedResponse<ProductResponse> getProductsByCategory(String categoryId, int page, int size) {
    log.info("Fetching products by category: {}", categoryId);

    categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Product> productPage = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
    return buildPagedResponse(productPage);
  }

  public PagedResponse<ProductResponse> searchProducts(String query, int page, int size) {
    log.info("Searching products with query: {}", query);
    Pageable pageable = PageRequest.of(page, size);
    Page<Product> productPage = productRepository.searchProducts(query, pageable);
    return buildPagedResponse(productPage);
  }

  public PagedResponse<ProductResponse> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice,
                                                           int page, int size) {
    log.info("Filtering products by price: {} - {}", minPrice, maxPrice);
    Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
    Page<Product> productPage = productRepository.findByPriceBetweenAndActiveTrue(minPrice, maxPrice, pageable);
    return buildPagedResponse(productPage);
  }

  public PagedResponse<ProductResponse> filterByBrand(String brand, int page, int size) {
    log.info("Filtering products by brand: {}", brand);
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Product> productPage = productRepository.findByBrandIgnoreCaseAndActiveTrue(brand, pageable);
    return buildPagedResponse(productPage);
  }

  public PagedResponse<ProductResponse> getFeaturedProducts(int page, int size) {
    log.info("Fetching featured products");
    Pageable pageable = PageRequest.of(page, size);
    Page<Product> productPage = productRepository.findByFeaturedTrueAndActiveTrue(pageable);
    return buildPagedResponse(productPage);
  }


  public ProductResponse updateProduct(String id, ProductRequest request) {
    log.info("Updating product: {}", id);

    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    String newSlug = request.getSlug();
    if (newSlug == null || newSlug.isBlank()) {
      newSlug = slugify.slugify(request.getName());
    }
    newSlug = ensureUniqueSlug(newSlug, id);

    if (request.getSku() != null && !request.getSku().isBlank()) {
      productRepository.findBySku(request.getSku())
          .ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
              throw new BadRequestException("Product with SKU '" + request.getSku() + "' already exists");
            }
          });
    }

    Integer discountPercentage = calculateDiscountPercentage(request.getPrice(), request.getDiscountPrice());

    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setSlug(newSlug);
    product.setSku(request.getSku());
    product.setPrice(request.getPrice());
    product.setDiscountPrice(request.getDiscountPrice());
    product.setDiscountPercentage(discountPercentage);
    product.setStock(request.getStock() != null ? request.getStock() : 0);
    product.setCategoryId(request.getCategoryId());
    product.setBrand(request.getBrand());
    product.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
    product.setActive(request.isActive());
    product.setFeatured(request.isFeatured());
    product.setDetails(mapProductDetails(request.getDetails()));

    Product updated = productRepository.save(product);
    log.info("Product updated: {}", updated.getId());

    return buildProductResponse(updated, category.getName());
  }


  public ProductResponse partialUpdateProduct(String id, ProductUpdateRequest request) {
    log.info("Partially updating product: {}", id);

    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

    String categoryName = getCategoryName(product.getCategoryId());

    if (request.getName() != null) {
      product.setName(request.getName());
      String newSlug = request.getSlug() != null ? request.getSlug() : slugify.slugify(request.getName());
      product.setSlug(ensureUniqueSlug(newSlug, id));
    }

    if (request.getDescription() != null) {
      product.setDescription(request.getDescription());
    }

    if (request.getSku() != null) {
      productRepository.findBySku(request.getSku())
          .ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
              throw new BadRequestException("Product with SKU '" + request.getSku() + "' already exists");
            }
          });
      product.setSku(request.getSku());
    }

    if (request.getPrice() != null) {
      product.setPrice(request.getPrice());
      BigDecimal discountPrice = request.getDiscountPrice() != null
          ? request.getDiscountPrice()
          : product.getDiscountPrice();
      Integer discountPercentage = calculateDiscountPercentage(request.getPrice(), discountPrice);
      product.setDiscountPercentage(discountPercentage);
    }

    if (request.getDiscountPrice() != null) {
      product.setDiscountPrice(request.getDiscountPrice());
      Integer discountPercentage = calculateDiscountPercentage(product.getPrice(), request.getDiscountPrice());
      product.setDiscountPercentage(discountPercentage);
    }

    if (request.getStock() != null) {
      product.setStock(request.getStock());
    }

    if (request.getCategoryId() != null) {
      Category category = categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
      product.setCategoryId(request.getCategoryId());
      categoryName = category.getName();
    }

    if (request.getBrand() != null) {
      product.setBrand(request.getBrand());
    }

    if (request.getTags() != null) {
      product.setTags(request.getTags());
    }

    if (request.getActive() != null) {
      product.setActive(request.getActive());
    }

    if (request.getFeatured() != null) {
      product.setFeatured(request.getFeatured());
    }

    if (request.getDetails() != null) {
      product.setDetails(mapUpdateProductDetails(request.getDetails(), product.getDetails()));
    }

    Product updated = productRepository.save(product);
    log.info("Product partially updated: {}", updated.getId());

    return buildProductResponse(updated, categoryName);
  }


  public void deleteProduct(String id) {
    log.info("Deleting product: {}", id);

    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

    if (product.getImages() != null && !product.getImages().isEmpty()) {
      cloudinaryService.deleteImages(product.getImages());
    }

    productRepository.delete(product);
    log.info("Product deleted: {}", id);
  }


  public ProductResponse addImage(String productId, String imageUrl) {
    log.info("Adding image to product: {}", productId);

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    if (product.getImages() == null) {
      product.setImages(new ArrayList<>());
    }
    product.getImages().add(imageUrl);

    Product updated = productRepository.save(product);
    return buildProductResponse(updated, getCategoryName(updated.getCategoryId()));
  }

  public ProductResponse removeImage(String productId, String imageUrl) {
    log.info("Removing image from product: {}", productId);

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    if (product.getImages() != null) {
      product.getImages().remove(imageUrl);
      cloudinaryService.deleteImage(imageUrl);
    }

    Product updated = productRepository.save(product);
    return buildProductResponse(updated, getCategoryName(updated.getCategoryId()));
  }


  private String ensureUniqueSlug(String slug, String excludeId) {
    String baseSlug = slug;
    int counter = 1;

    while (true) {
      var existing = productRepository.findBySlug(slug);
      if (existing.isEmpty() || (excludeId != null && existing.get().getId().equals(excludeId))) {
        break;
      }
      slug = baseSlug + "-" + counter++;
    }

    return slug;
  }

  private Integer calculateDiscountPercentage(BigDecimal price, BigDecimal discountPrice) {
    if (price == null || discountPrice == null || discountPrice.compareTo(BigDecimal.ZERO) <= 0) {
      return null;
    }
    if (discountPrice.compareTo(price) >= 0) {
      return null;
    }

    BigDecimal discount = price.subtract(discountPrice);
    BigDecimal percentage = discount.multiply(BigDecimal.valueOf(100))
        .divide(price, 0, RoundingMode.HALF_UP);
    return percentage.intValue();
  }

  private String getCategoryName(String categoryId) {
    if (categoryId == null) return null;
    return categoryRepository.findById(categoryId)
        .map(Category::getName)
        .orElse(null);
  }

  private Product.ProductDetails mapProductDetails(ProductRequest.ProductDetailsRequest request) {
    if (request == null) return null;

    return Product.ProductDetails.builder()
        .weight(request.getWeight())
        .dimensions(request.getDimensions())
        .color(request.getColor())
        .size(request.getSize())
        .material(request.getMaterial())
        .specifications(request.getSpecifications())
        .build();
  }

  private Product.ProductDetails mapUpdateProductDetails(
      ProductUpdateRequest.ProductDetailsRequest request,
      Product.ProductDetails existing) {

    if (existing == null) {
      existing = new Product.ProductDetails();
    }

    if (request.getWeight() != null) existing.setWeight(request.getWeight());
    if (request.getDimensions() != null) existing.setDimensions(request.getDimensions());
    if (request.getColor() != null) existing.setColor(request.getColor());
    if (request.getSize() != null) existing.setSize(request.getSize());
    if (request.getMaterial() != null) existing.setMaterial(request.getMaterial());
    if (request.getSpecifications() != null) existing.setSpecifications(request.getSpecifications());

    return existing;
  }

  private ProductResponse buildProductResponse(Product product, String categoryName) {
    return ProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .slug(product.getSlug())
        .sku(product.getSku())
        .price(product.getPrice())
        .discountPrice(product.getDiscountPrice())
        .discountPercentage(product.getDiscountPercentage())
        .stock(product.getStock())
        .inStock(product.getStock() != null && product.getStock() > 0)
        .categoryId(product.getCategoryId())
        .categoryName(categoryName)
        .brand(product.getBrand())
        .images(product.getImages())
        .tags(product.getTags())
        .active(product.isActive())
        .featured(product.isFeatured())
        .details(mapProductDetailsResponse(product.getDetails()))
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }

  private ProductResponse.ProductDetailsResponse mapProductDetailsResponse(Product.ProductDetails details) {
    if (details == null) return null;

    return ProductResponse.ProductDetailsResponse.builder()
        .weight(details.getWeight())
        .dimensions(details.getDimensions())
        .color(details.getColor())
        .size(details.getSize())
        .material(details.getMaterial())
        .specifications(details.getSpecifications())
        .build();
  }

  private PagedResponse<ProductResponse> buildPagedResponse(Page<Product> productPage) {
    List<ProductResponse> content = productPage.getContent().stream()
        .map(p -> buildProductResponse(p, getCategoryName(p.getCategoryId())))
        .collect(Collectors.toList());

    return PagedResponse.of(
        content,
        productPage.getNumber(),
        productPage.getSize(),
        productPage.getTotalElements(),
        productPage.getTotalPages()
    );
  }
}