package com.shopzone.orderservice.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CancelOrderRequest {
    @NotBlank private String reason;
}
