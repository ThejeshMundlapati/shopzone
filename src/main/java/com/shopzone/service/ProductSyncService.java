package com.shopzone.service;

import com.shopzone.model.Category;
import com.shopzone.model.Product;
import com.shopzone.model.elasticsearch.ProductDocument;
import com.shopzone.repository.elasticsearch.ProductSearchRepository;
import com.shopzone.repository.mongo.CategoryRepository;
import com.shopzone.repository.mongo.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSyncService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductSearchRepository productSearchRepository;

  /**
   * Sync a single product to Elasticsearch
   */
  public void syncProduct(Product product) {
    try {
      ProductDocument document = convertToDocument(product);
      productSearchRepository.save(document);
      log.debug("Synced product {} to Elasticsearch", product.getId());
    } catch (Exception e) {
      log.error("Failed to sync product {} to Elasticsearch: {}", product.getId(), e.getMessage());
    }
  }

  /**
   * Sync product by ID
   */
  public void syncProductById(String productId) {
    productRepository.findById(productId).ifPresent(this::syncProduct);
  }

  /**
   * Remove product from Elasticsearch
   */
  public void removeProduct(String productId) {
    try {
      productSearchRepository.deleteById(productId);
      log.debug("Removed product {} from Elasticsearch", productId);
    } catch (Exception e) {
      log.error("Failed to remove product {} from Elasticsearch: {}", productId, e.getMessage());
    }
  }

  /**
   * Update product rating in Elasticsearch
   */
  public void updateProductRating(String productId, Double averageRating, Integer reviewCount) {
    try {
      Optional<ProductDocument> docOpt = productSearchRepository.findById(productId);
      if (docOpt.isPresent()) {
        ProductDocument doc = docOpt.get();
        doc.setAverageRating(averageRating != null ? averageRating : 0.0);
        doc.setReviewCount(reviewCount != null ? reviewCount : 0);
        doc.setUpdatedAt(LocalDateTime.now());
        productSearchRepository.save(doc);
        log.debug("Updated rating for product {} in Elasticsearch", productId);
      } else {
        syncProductById(productId);
      }
    } catch (Exception e) {
      log.error("Failed to update product rating in Elasticsearch: {}", e.getMessage());
    }
  }

  /**
   * Full sync: Sync all products from MongoDB to Elasticsearch
   */
  @Async
  public void syncAllProducts() {
    log.info("Starting full product sync to Elasticsearch...");
    long startTime = System.currentTimeMillis();
    int count = 0;

    try {
      productSearchRepository.deleteAll();
      log.info("Cleared existing Elasticsearch index");

      List<Product> products = productRepository.findByActiveTrue();

      List<ProductDocument> documents = products.stream()
          .map(this::convertToDocument)
          .collect(Collectors.toList());

      productSearchRepository.saveAll(documents);
      count = documents.size();

      long duration = System.currentTimeMillis() - startTime;
      log.info("Full sync completed: {} products indexed in {}ms", count, duration);
    } catch (Exception e) {
      log.error("Full sync failed: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to sync products to Elasticsearch", e);
    }
  }

  /**
   * Incremental sync: Sync products updated after a certain time
   */
  @Async
  public void incrementalSync(LocalDateTime since) {
    log.info("Starting incremental sync for products updated since {}", since);

    try {
      List<Product> updatedProducts = productRepository.findByUpdatedAtAfter(since);

      for (Product product : updatedProducts) {
        if (product.isActive()) {
          syncProduct(product);
        } else {
          removeProduct(product.getId());
        }
      }

      log.info("Incremental sync completed: {} products processed", updatedProducts.size());
    } catch (Exception e) {
      log.error("Incremental sync failed: {}", e.getMessage());
    }
  }

  /**
   * Get sync status
   */
  public SyncStatus getSyncStatus() {
    long mongoCount = productRepository.countByActiveTrue();
    long esCount = productSearchRepository.count();

    return new SyncStatus(mongoCount, esCount, mongoCount == esCount);
  }

  /**
   * Reindex a specific category's products
   */
  public void reindexCategory(String categoryId) {
    List<Product> products = productRepository.findByCategoryId(categoryId);
    for (Product product : products) {
      if (product.isActive()) {
        syncProduct(product);
      }
    }
    log.info("Reindexed {} products for category {}", products.size(), categoryId);
  }


  private ProductDocument convertToDocument(Product product) {
    String categoryName = null;
    String categorySlug = null;

    if (product.getCategoryId() != null) {
      Optional<Category> categoryOpt = categoryRepository.findById(product.getCategoryId());
      if (categoryOpt.isPresent()) {
        Category category = categoryOpt.get();
        categoryName = category.getName();
        categorySlug = category.getSlug();
      }
    }

    List<String> suggestions = Arrays.asList(
        product.getName(),
        product.getBrand()
    );

    ProductDocument.Completion nameSuggest = ProductDocument.Completion.builder()
        .input(suggestions.stream().filter(s -> s != null).collect(Collectors.toList()))
        .weight(product.getStock() != null && product.getStock() > 0 ? 10 : 1)
        .build();

    Double price = product.getPrice() != null ? product.getPrice().doubleValue() : null;
    Double discountPrice = product.getDiscountPrice() != null ? product.getDiscountPrice().doubleValue() : null;

    return ProductDocument.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .sku(product.getSku())
        .slug(product.getSlug())
        .brand(product.getBrand())
        .price(price)
        .salePrice(discountPrice)
        .stock(product.getStock())
        .active(product.isActive())
        .categoryId(product.getCategoryId())
        .categoryName(categoryName)
        .categorySlug(categorySlug)
        .tags(product.getTags())
        .images(product.getImages())
        .averageRating(product.getAverageRating() != null ? product.getAverageRating() : 0.0)
        .reviewCount(product.getReviewCount() != null ? product.getReviewCount() : 0)
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .nameSuggest(nameSuggest)
        .build();
  }

  public record SyncStatus(long mongoCount, long elasticsearchCount, boolean inSync) {}
}