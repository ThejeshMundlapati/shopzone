package com.shopzone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Daily revenue breakdown entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRevenueEntry {

  private LocalDate date;
  private BigDecimal revenue;
  private int orderCount;
}