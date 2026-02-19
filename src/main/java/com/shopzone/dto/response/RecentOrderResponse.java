package com.shopzone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Recent order entry for dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecentOrderResponse {

  private String orderNumber;
  private String customerName;
  private String customerEmail;
  private String status;
  private String statusDisplayName;
  private String paymentStatus;
  private BigDecimal totalAmount;
  private int itemCount;
  private LocalDateTime createdAt;
}