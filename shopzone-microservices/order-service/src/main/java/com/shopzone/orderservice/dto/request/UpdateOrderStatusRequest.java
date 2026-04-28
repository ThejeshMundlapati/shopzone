package com.shopzone.orderservice.dto.request;
import com.shopzone.orderservice.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateOrderStatusRequest {
    @NotNull private OrderStatus status;
    private String trackingNumber;
    private String shippingCarrier;
    private String adminNotes;
}
