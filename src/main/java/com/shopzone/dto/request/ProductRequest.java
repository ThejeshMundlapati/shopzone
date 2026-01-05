package com.shopzone.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

  @NotBlank(message = "Product name is required")
  @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
  private String name;

  @Size(max = 2000, message = "Description must not exceed 2000 characters")
  private String description;

  private String slug;  // Optional - auto-generated from name

  private String sku;   // Optional - Stock Keeping Unit

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.01", message = "Price must be greater than 0")
  private BigDecimal price;

  @DecimalMin(value = "0.00", message = "Discount price cannot be negative")
  private BigDecimal discountPrice;

  @Min(value = 0, message = "Stock cannot be negative")
  @Builder.Default
  private Integer stock = 0;

  @NotBlank(message = "Category ID is required")
  private String categoryId;

  @Size(max = 100, message = "Brand name must not exceed 100 characters")
  private String brand;

  private List<String> tags;

  @Builder.Default
  private boolean active = true;

  @Builder.Default
  private boolean featured = false;

  private ProductDetailsRequest details;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProductDetailsRequest {
    private String weight;
    private String dimensions;
    private String color;
    private String size;
    private String material;
    private Map<String, String> specifications;
  }
}
