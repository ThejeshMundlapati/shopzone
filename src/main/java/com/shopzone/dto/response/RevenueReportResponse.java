package com.shopzone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Revenue report response with daily breakdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RevenueReportResponse {

  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal totalRevenue;
  private long totalOrders;
  private BigDecimal totalTax;
  private BigDecimal totalShipping;
  private BigDecimal totalDiscount;
  private BigDecimal averageOrderValue;
  private List<DailyRevenueEntry> dailyRevenue;
}