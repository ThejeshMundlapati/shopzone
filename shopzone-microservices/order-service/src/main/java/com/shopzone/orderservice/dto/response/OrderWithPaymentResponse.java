package com.shopzone.orderservice.dto.response;
import lombok.*;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderWithPaymentResponse {
    private OrderResponse order;
    private Map<String, Object> payment;
}
