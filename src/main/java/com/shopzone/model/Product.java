package com.shopzone.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

  @Id
  private String id;

  @TextIndexed(weight = 10)
  private String name;

  @TextIndexed(weight = 5)
  private String description;

  @Indexed(unique = true)
  private String slug;

  @Indexed(unique = true, sparse = true)
  private String sku;

  private BigDecimal price;

  private BigDecimal discountPrice;

  private Integer discountPercentage;

  @Builder.Default
  private Integer stock = 0;

  @Indexed
  private String categoryId;

  @Indexed
  private String brand;

  @Builder.Default
  private List<String> images = new ArrayList<>();

  @Builder.Default
  private List<String> tags = new ArrayList<>();

  @Builder.Default
  private boolean active = true;

  @Builder.Default
  private boolean featured = false;

  private ProductDetails details;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProductDetails {
    private String weight;
    private String dimensions;
    private String color;
    private String size;
    private String material;
    private Map<String, String> specifications;
  }
}