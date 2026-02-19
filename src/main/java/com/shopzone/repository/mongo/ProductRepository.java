package com.shopzone.repository.mongo;

import com.shopzone.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {


  Optional<Product> findByIdAndActiveTrue(String id);

  Optional<Product> findBySlug(String slug);

  Optional<Product> findBySku(String sku);

  boolean existsBySlug(String slug);

  boolean existsBySku(String sku);

  Page<Product> findByCategoryIdAndActiveTrue(String categoryId, Pageable pageable);

  List<Product> findByCategoryId(String categoryId);

  long countByCategoryId(String categoryId);

  Page<Product> findByBrandIgnoreCaseAndActiveTrue(String brand, Pageable pageable);

  List<String> findDistinctBrandByActiveTrue();

  @Query("{ '$or': [ " +
      "{ 'name': { '$regex': ?0, '$options': 'i' } }, " +
      "{ 'description': { '$regex': ?0, '$options': 'i' } }, " +
      "{ 'brand': { '$regex': ?0, '$options': 'i' } }, " +
      "{ 'tags': { '$regex': ?0, '$options': 'i' } } " +
      "], 'active': true }")
  Page<Product> searchProducts(String searchTerm, Pageable pageable);

  Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

  @Query("{ 'price': { '$gte': ?0, '$lte': ?1 }, 'active': true }")
  Page<Product> findByPriceBetweenAndActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

  Page<Product> findByPriceGreaterThanEqualAndActiveTrue(BigDecimal minPrice, Pageable pageable);

  Page<Product> findByPriceLessThanEqualAndActiveTrue(BigDecimal maxPrice, Pageable pageable);

  Page<Product> findByTagsContainingAndActiveTrue(String tag, Pageable pageable);

  Page<Product> findByFeaturedTrueAndActiveTrue(Pageable pageable);

  Page<Product> findByActiveTrue(Pageable pageable);

  List<Product> findByActiveTrueOrderByCreatedAtDesc();

  @Query("{ 'categoryId': ?0, 'price': { '$gte': ?1, '$lte': ?2 }, 'active': true }")
  Page<Product> findByCategoryAndPriceRange(String categoryId, BigDecimal minPrice,
                                            BigDecimal maxPrice, Pageable pageable);

  @Query("{ 'brand': { '$regex': ?0, '$options': 'i' }, 'categoryId': ?1, 'active': true }")
  Page<Product> findByBrandAndCategory(String brand, String categoryId, Pageable pageable);

  List<Product> findByStockLessThanAndActiveTrue(int threshold);

  List<Product> findByStockEqualsAndActiveTrue(int stock);

  List<Product> findByIdIn(List<String> ids);

  @Query("{ '_id': ?0, 'stock': { $gte: ?1 }, 'active': true }")
  Optional<Product> findByIdWithSufficientStock(String id, int requiredQuantity);

  @Query("{ '_id': ?0, 'stock': { $gte: ?1 } }")
  @Update("{ '$inc': { 'stock': ?2 } }")
  int reduceStock(String productId, int requiredStock, int reduceBy);

  @Query("{ '_id': ?0 }")
  @Update("{ '$inc': { 'stock': ?1 } }")
  int increaseStock(String productId, int increaseBy);

  @Query("{ 'active': true, 'stock': { $lte: ?0 } }")
  List<Product> findLowStockProducts(int threshold);

  @Query("{ 'active': true, 'stock': 0 }")
  List<Product> findOutOfStockProducts();

  @Query(value = "{ 'active': true, 'stock': 0 }", count = true)
  long countOutOfStock();

  @Query(value = "{ 'active': true, 'stock': { $gt: 0, $lte: ?0 } }", count = true)
  long countLowStock(int threshold);


  List<Product> findByActiveTrue();

  long countByActiveTrue();

  List<Product> findByUpdatedAtAfter(LocalDateTime since);

  Page<Product> findByAverageRatingGreaterThanEqualAndActiveTrue(Double minRating, Pageable pageable);

  @Query("{ 'active': true, 'reviewCount': { $gt: 0 } }")
  Page<Product> findTopRatedProducts(Pageable pageable);


  /**
   * Count out-of-stock products (stock = 0).
   */
  @Query(value = "{ 'stock': 0 }", count = true)
  long countByStockEquals(int stock);

  /**
   * Count low stock products (stock between 1 and threshold).
   */
  @Query(value = "{ 'stock': { '$gt': 0, '$lte': ?0 } }", count = true)
  long countLowStockProducts(int threshold);
}