package com.shopzone.repository;

import com.shopzone.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {


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
}