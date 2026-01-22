package com.shopzone.dto.response;

import com.shopzone.model.Order;
import com.shopzone.model.OrderItem;
import com.shopzone.model.enums.OrderStatus;
import com.shopzone.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Summary response DTO for order list views.
 * Contains essential information without full details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {

  private String id;
  private String orderNumber;

  private OrderStatus status;
  private String statusDisplayName;
  private PaymentStatus paymentStatus;

  private int itemCount;
  private int uniqueItemCount;
  private BigDecimal totalAmount;

  /**
   * First product image for visual representation.
   */
  private String previewImage;

  /**
   * First product name for preview.
   */
  private String previewProductName;

  /**
   * Additional items count (e.g., "+3 more items").
   */
  private int additionalItemsCount;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /**
   * Whether the order can still be cancelled.
   */
  private boolean canCancel;

  /**
   * Create summary from Order entity.
   */
  public static OrderSummaryResponse fromEntity(Order order) {
    OrderItem firstItem = order.getItems().isEmpty() ? null : order.getItems().get(0);

    return OrderSummaryResponse.builder()
        .id(order.getId())
        .orderNumber(order.getOrderNumber())
        .status(order.getStatus())
        .statusDisplayName(order.getStatus().getDisplayName())
        .paymentStatus(order.getPaymentStatus())
        .itemCount(order.getTotalItemCount())
        .uniqueItemCount(order.getUniqueItemCount())
        .totalAmount(order.getTotalAmount())
        .previewImage(firstItem != null ? firstItem.getProductImage() : null)
        .previewProductName(firstItem != null ? firstItem.getProductName() : null)
        .additionalItemsCount(Math.max(0, order.getUniqueItemCount() - 1))
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .canCancel(order.canCancel())
        .build();
  }
}