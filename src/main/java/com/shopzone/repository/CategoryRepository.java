package com.shopzone.repository;

import com.shopzone.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

  Optional<Category> findBySlug(String slug);

  Optional<Category> findByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCase(String name);

  boolean existsBySlug(String slug);

  List<Category> findByActiveTrueOrderByDisplayOrderAsc();

  List<Category> findByParentIdIsNullAndActiveTrueOrderByDisplayOrderAsc();

  List<Category> findByParentIdAndActiveTrueOrderByDisplayOrderAsc(String parentId);

  List<Category> findByParentId(String parentId);

  long countByParentId(String parentId);
}