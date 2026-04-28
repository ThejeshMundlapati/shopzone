package com.shopzone.searchservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(indexName = "products")
@Setting(shards = 1, replicas = 0)
public class ProductDocument {
    @Id private String id;
    @Field(type = FieldType.Text, analyzer = "standard") private String name;
    @Field(type = FieldType.Text) private String description;
    @Field(type = FieldType.Keyword) private String sku, slug, brand, categoryId, categoryName, categorySlug;
    @Field(type = FieldType.Double) private Double price, salePrice;
    @Field(type = FieldType.Integer) private Integer stock;
    @Field(type = FieldType.Boolean) private boolean active;
    @Field(type = FieldType.Keyword) private List<String> tags;
    @Field(type = FieldType.Keyword) private List<String> images;
    @Field(type = FieldType.Double) private Double averageRating;
    @Field(type = FieldType.Integer) private Integer reviewCount;
    @Field(type = FieldType.Date) private LocalDateTime createdAt, updatedAt;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Completion {
        private List<String> input;
        private int weight;
    }
    private Completion nameSuggest;
}
