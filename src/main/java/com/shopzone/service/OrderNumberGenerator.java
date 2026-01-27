package com.shopzone.service;

import com.shopzone.repository.jpa.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating unique, human-readable order numbers.
 *
 * Format: ORD-YYYYMMDD-XXXX
 * Example: ORD-20240119-A7K2
 *
 * Benefits:
 * - Human-readable and easy to communicate
 * - Date prefix allows quick identification of order age
 * - Random suffix ensures uniqueness
 * - Short enough to read over phone
 */
@Service
@RequiredArgsConstructor
public class OrderNumberGenerator {

  private final OrderRepository orderRepository;

  private static final String PREFIX = "ORD";
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final String ALPHANUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Excluding confusing chars: I,O,0,1
  private static final int SUFFIX_LENGTH = 4;
  private static final int MAX_ATTEMPTS = 10;

  private final SecureRandom random = new SecureRandom();

  /**
   * Generate a unique order number.
   *
   * @return Unique order number in format ORD-YYYYMMDD-XXXX
   * @throws RuntimeException if unable to generate unique number after max attempts
   */
  public String generate() {
    String datePart = LocalDate.now().format(DATE_FORMAT);

    for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
      String suffix = generateRandomSuffix();
      String orderNumber = String.format("%s-%s-%s", PREFIX, datePart, suffix);

      if (!orderRepository.existsByOrderNumber(orderNumber)) {
        return orderNumber;
      }
    }

    String suffix = generateRandomSuffix();
    long timestamp = System.currentTimeMillis() % 10000;
    return String.format("%s-%s-%s%d", PREFIX, datePart, suffix, timestamp);
  }

  /**
   * Generate random alphanumeric suffix.
   */
  private String generateRandomSuffix() {
    StringBuilder sb = new StringBuilder(SUFFIX_LENGTH);
    for (int i = 0; i < SUFFIX_LENGTH; i++) {
      int index = random.nextInt(ALPHANUMERIC.length());
      sb.append(ALPHANUMERIC.charAt(index));
    }
    return sb.toString();
  }

  /**
   * Validate order number format.
   */
  public boolean isValidFormat(String orderNumber) {
    if (orderNumber == null || orderNumber.isBlank()) {
      return false;
    }

    return orderNumber.matches("^ORD-\\d{8}-[A-Z0-9]{4,}$");
  }

  /**
   * Extract date from order number.
   */
  public LocalDate extractDate(String orderNumber) {
    if (!isValidFormat(orderNumber)) {
      throw new IllegalArgumentException("Invalid order number format: " + orderNumber);
    }

    String datePart = orderNumber.substring(4, 12);
    return LocalDate.parse(datePart, DATE_FORMAT);
  }
}