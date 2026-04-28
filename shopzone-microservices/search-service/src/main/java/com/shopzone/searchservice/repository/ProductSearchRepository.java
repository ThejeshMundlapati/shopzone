package com.shopzone.searchservice.repository;
import com.shopzone.searchservice.model.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {}
