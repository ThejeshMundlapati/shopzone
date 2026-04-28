package com.shopzone.cartservice.dto.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddToCartRequest {
    @NotBlank private String productId;
    @Min(1) @Builder.Default private int quantity = 1;
}
