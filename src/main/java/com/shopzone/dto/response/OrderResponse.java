package com.shopzone.dto.response;

import com.shopzone.model.AddressSnapshot;
import com.shopzone.model.Order;
import com.shopzone.model.enums.OrderStatus;
import com.shopzone.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Full order details response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

  private String id;
  private String orderNumber;

  private OrderStatus status;
  private String statusDisplayName;
  private String statusDescription;
  private PaymentStatus paymentStatus;
  private String paymentStatusDisplayName;

  private String userId;
  private String userEmail;
  private String userFullName;

  private ShippingAddressResponse shippingAddress;
  private String trackingNumber;
  private String shippingCarrier;

  private List<OrderItemResponse> items;
  private int totalItemCount;
  private int uniqueItemCount;

  private BigDecimal subtotal;
  private BigDecimal taxRate;
  private BigDecimal taxAmount;
  private BigDecimal shippingCost;
  private BigDecimal discountAmount;
  private BigDecimal totalAmount;
  private BigDecimal totalSavings;

  private String customerNotes;
  private String cancellationReason;
  private String cancelledBy;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime paidAt;
  private LocalDateTime confirmedAt;
  private LocalDateTime shippedAt;
  private LocalDateTime deliveredAt;
  private LocalDateTime cancelledAt;

  private boolean canCancel;
  private boolean canReturn;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ShippingAddressResponse {
    private String fullName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String landmark;
    private String formattedAddress;

    public static ShippingAddressResponse fromSnapshot(AddressSnapshot snapshot) {
      if (snapshot == null) return null;

      return ShippingAddressResponse.builder()
          .fullName(snapshot.getFullName())
          .phoneNumber(snapshot.getPhoneNumber())
          .addressLine1(snapshot.getAddressLine1())
          .addressLine2(snapshot.getAddressLine2())
          .city(snapshot.getCity())
          .state(snapshot.getState())
          .postalCode(snapshot.getPostalCode())
          .country(snapshot.getCountry())
          .landmark(snapshot.getLandmark())
          .formattedAddress(snapshot.getFormattedAddress())
          .build();
    }
  }

  public static OrderResponse fromEntity(Order order) {
    return OrderResponse.builder()
        .id(order.getId())
        .orderNumber(order.getOrderNumber())
        .status(order.getStatus())
        .statusDisplayName(order.getStatus().getDisplayName())
        .statusDescription(order.getStatus().getDescription())
        .paymentStatus(order.getPaymentStatus())
        .paymentStatusDisplayName(order.getPaymentStatus().getDisplayName())
        .userId(order.getUserId())
        .userEmail(order.getUserEmail())
        .userFullName(order.getUserFullName())
        .shippingAddress(ShippingAddressResponse.fromSnapshot(order.getShippingAddress()))
        .trackingNumber(order.getTrackingNumber())
        .shippingCarrier(order.getShippingCarrier())
        .items(order.getItems().stream()
            .map(OrderItemResponse::fromEntity)
            .collect(Collectors.toList()))
        .totalItemCount(order.getTotalItemCount())
        .uniqueItemCount(order.getUniqueItemCount())
        .subtotal(order.getSubtotal())
        .taxRate(order.getTaxRate())
        .taxAmount(order.getTaxAmount())
        .shippingCost(order.getShippingCost())
        .discountAmount(order.getDiscountAmount())
        .totalAmount(order.getTotalAmount())
        .totalSavings(order.getTotalItemSavings())
        .customerNotes(order.getCustomerNotes())
        .cancellationReason(order.getCancellationReason())
        .cancelledBy(order.getCancelledBy())
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .paidAt(order.getPaidAt())
        .confirmedAt(order.getConfirmedAt())
        .shippedAt(order.getShippedAt())
        .deliveredAt(order.getDeliveredAt())
        .cancelledAt(order.getCancelledAt())
        .canCancel(order.canCancel())
        .canReturn(order.getStatus() == OrderStatus.DELIVERED)
        .build();
  }
}