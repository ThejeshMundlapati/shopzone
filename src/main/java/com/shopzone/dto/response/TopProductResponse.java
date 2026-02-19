package com.shopzone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Top selling product entry for dashboard/reports.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopProductResponse {

  private String productId;
  private String productName;
  private String productImage;
  private int totalQuantitySold;
  private BigDecimal totalRevenue;
  private int orderCount;
}