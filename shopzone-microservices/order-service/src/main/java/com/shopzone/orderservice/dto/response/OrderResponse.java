package com.shopzone.orderservice.dto.response;
import com.shopzone.orderservice.model.Order;
import com.shopzone.orderservice.model.enums.OrderStatus;
import com.shopzone.orderservice.model.enums.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderResponse {
    private String id, orderNumber, userId, userEmail, userFullName;
    private OrderStatus status;
    private String statusDisplayName;
    private PaymentStatus paymentStatus;
    private List<OrderItemResponse> items;
    private BigDecimal subtotal, taxRate, taxAmount, shippingCost, discountAmount, totalAmount, amountRefunded;
    private String trackingNumber, shippingCarrier, customerNotes, cancellationReason;
    private String receiptUrl, stripePaymentIntentId;
    private int totalItems;
    private LocalDateTime createdAt, updatedAt, paidAt, confirmedAt, shippedAt, deliveredAt, cancelledAt;
    private AddressSnapshotResponse shippingAddress;

    public static OrderResponse fromEntity(Order o) {
        return OrderResponse.builder()
            .id(o.getId()).orderNumber(o.getOrderNumber()).userId(o.getUserId())
            .userEmail(o.getUserEmail()).userFullName(o.getUserFullName())
            .status(o.getStatus()).statusDisplayName(o.getStatus().getDisplayName())
            .paymentStatus(o.getPaymentStatus())
            .items(o.getItems() != null ? o.getItems().stream().map(OrderItemResponse::fromEntity).collect(Collectors.toList()) : List.of())
            .subtotal(o.getSubtotal()).taxRate(o.getTaxRate()).taxAmount(o.getTaxAmount())
            .shippingCost(o.getShippingCost()).discountAmount(o.getDiscountAmount())
            .totalAmount(o.getTotalAmount()).amountRefunded(o.getAmountRefunded())
            .trackingNumber(o.getTrackingNumber()).shippingCarrier(o.getShippingCarrier())
            .customerNotes(o.getCustomerNotes()).cancellationReason(o.getCancellationReason())
            .receiptUrl(o.getReceiptUrl()).stripePaymentIntentId(o.getStripePaymentIntentId())
            .totalItems(o.getTotalItemCount())
            .createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt()).paidAt(o.getPaidAt())
            .confirmedAt(o.getConfirmedAt()).shippedAt(o.getShippedAt())
            .deliveredAt(o.getDeliveredAt()).cancelledAt(o.getCancelledAt())
            .shippingAddress(o.getShippingAddress() != null ? AddressSnapshotResponse.from(o.getShippingAddress()) : null)
            .build();
    }
}
