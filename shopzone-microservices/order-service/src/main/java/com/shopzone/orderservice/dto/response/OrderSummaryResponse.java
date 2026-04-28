package com.shopzone.orderservice.dto.response;
import com.shopzone.orderservice.model.Order;
import com.shopzone.orderservice.model.enums.OrderStatus;
import com.shopzone.orderservice.model.enums.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderSummaryResponse {
    private String orderNumber, userFullName, userEmail;
    private OrderStatus status;
    private String statusDisplayName;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private int totalItems;
    private LocalDateTime createdAt;

    public static OrderSummaryResponse fromEntity(Order o) {
        return OrderSummaryResponse.builder()
            .orderNumber(o.getOrderNumber()).userFullName(o.getUserFullName()).userEmail(o.getUserEmail())
            .status(o.getStatus()).statusDisplayName(o.getStatus().getDisplayName())
            .paymentStatus(o.getPaymentStatus()).totalAmount(o.getTotalAmount())
            .totalItems(o.getTotalItemCount()).createdAt(o.getCreatedAt()).build();
    }
}
