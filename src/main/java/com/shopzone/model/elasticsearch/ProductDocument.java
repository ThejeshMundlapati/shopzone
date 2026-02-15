package com.shopzone.model.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

  @Id
  private String id;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String name;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String description;

  @Field(type = FieldType.Keyword)
  private String sku;

  @Field(type = FieldType.Keyword)
  private String slug;

  @Field(type = FieldType.Keyword)
  private String brand;

  @Field(type = FieldType.Double)
  private Double price;

  @Field(type = FieldType.Double)
  private Double salePrice;

  @Field(type = FieldType.Integer)
  private Integer stock;

  @Field(type = FieldType.Boolean)
  private Boolean active;

  @Field(type = FieldType.Keyword)
  private String categoryId;

  @Field(type = FieldType.Keyword)
  private String categoryName;

  @Field(type = FieldType.Keyword)
  private String categorySlug;

  @Field(type = FieldType.Keyword)
  private List<String> tags;

  @Field(type = FieldType.Keyword)
  private List<String> images;

  @Field(type = FieldType.Double)
  private Double averageRating;

  @Field(type = FieldType.Integer)
  private Integer reviewCount;

  @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
  private LocalDateTime createdAt;

  @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
  private LocalDateTime updatedAt;

  @CompletionField(maxInputLength = 100)
  private Completion nameSuggest;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Completion {
    private List<String> input;
    private Integer weight;
  }
}