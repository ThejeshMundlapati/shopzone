package com.shopzone.dto.response;

import com.shopzone.model.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

  private String userId;
  private List<CartItemResponse> items;
  private int totalItems;
  private int uniqueItemCount;
  private BigDecimal subtotal;
  private BigDecimal totalSavings;
  private boolean isEmpty;
  private boolean hasInvalidItems;
  private List<CartItemResponse> invalidItems;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static CartResponse fromCart(Cart cart) {
    List<CartItemResponse> itemResponses = cart.getItems().stream()
        .map(CartItemResponse::fromCartItem)
        .toList();

    List<CartItemResponse> invalidItemResponses = cart.getInvalidItems().stream()
        .map(CartItemResponse::fromCartItem)
        .toList();

    return CartResponse.builder()
        .userId(cart.getUserId())
        .items(itemResponses)
        .totalItems(cart.getTotalItems())
        .uniqueItemCount(cart.getUniqueItemCount())
        .subtotal(cart.getSubtotal())
        .totalSavings(cart.getTotalSavings())
        .isEmpty(cart.isEmpty())
        .hasInvalidItems(!invalidItemResponses.isEmpty())
        .invalidItems(invalidItemResponses)
        .createdAt(cart.getCreatedAt())
        .updatedAt(cart.getUpdatedAt())
        .build();
  }
}