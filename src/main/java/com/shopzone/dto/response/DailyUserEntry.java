package com.shopzone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Daily user registration entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyUserEntry {

  private LocalDate date;
  private long newUsers;
}