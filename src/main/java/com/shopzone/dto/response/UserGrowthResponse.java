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
 * User growth report response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserGrowthResponse {

  private LocalDate startDate;
  private LocalDate endDate;
  private long totalUsers;
  private long newUsersInPeriod;
  private Map<String, Long> usersByRole;
  private List<DailyUserEntry> dailyRegistrations;
  private long verifiedUsers;
  private BigDecimal verificationRate;
}