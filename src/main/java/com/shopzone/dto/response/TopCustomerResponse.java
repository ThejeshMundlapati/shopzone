package com.shopzone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Top customer entry for dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopCustomerResponse {

  private String userId;
  private String customerName;
  private String customerEmail;
  private int totalOrders;
  private BigDecimal totalSpent;
  private BigDecimal averageOrderValue;
}