package com.shopzone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Sales report response with order breakdowns and top products.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalesReportResponse {

  private LocalDate startDate;
  private LocalDate endDate;
  private long totalOrders;
  private Map<String, Long> ordersByStatus;
  private Map<String, Long> ordersByPaymentStatus;
  private List<TopProductResponse> topProducts;
  private BigDecimal cancellationRate;
  private long cancelledOrders;
}