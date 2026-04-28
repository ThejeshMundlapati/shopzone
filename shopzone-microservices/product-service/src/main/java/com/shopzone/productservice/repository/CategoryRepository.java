package com.shopzone.productservice.repository;

import com.shopzone.productservice.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findBySlug(String slug);
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
    List<Category> findByParentIdIsNullAndActiveTrueOrderByDisplayOrderAsc();
    List<Category> findByParentIdAndActiveTrueOrderByDisplayOrderAsc(String parentId);
    long countByParentId(String parentId);
}
