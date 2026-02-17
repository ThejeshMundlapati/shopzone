package com.shopzone.repository.elasticsearch;

import com.shopzone.model.elasticsearch.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

  Page<ProductDocument> findByNameContainingIgnoreCase(String name, Pageable pageable);

  @Query("{\"bool\": {\"should\": [{\"match\": {\"name\": \"?0\"}}, {\"match\": {\"description\": \"?0\"}}]}}")
  Page<ProductDocument> searchByNameOrDescription(String query, Pageable pageable);

  Page<ProductDocument> findByCategoryId(String categoryId, Pageable pageable);

  Page<ProductDocument> findByBrand(String brand, Pageable pageable);

  Page<ProductDocument> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

  Page<ProductDocument> findByAverageRatingGreaterThanEqual(Double minRating, Pageable pageable);

  Page<ProductDocument> findByStockGreaterThan(Integer minStock, Pageable pageable);

  Page<ProductDocument> findByActiveTrue(Pageable pageable);

  Page<ProductDocument> findByTagsContaining(String tag, Pageable pageable);

  Page<ProductDocument> findByCategoryIdAndActiveTrue(String categoryId, Pageable pageable);

  Page<ProductDocument> findByBrandAndActiveTrue(String brand, Pageable pageable);

  boolean existsById(String id);

  @Query("{\"size\": 0, \"aggs\": {\"brands\": {\"terms\": {\"field\": \"brand\", \"size\": 100}}}}")
  List<String> findAllBrands();
}