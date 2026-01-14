// src/main/java/com/shopzone/dto/response/WishlistResponse.java

package com.shopzone.dto.response;

import com.shopzone.model.Wishlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {

  private String userId;
  private List<WishlistItemResponse> items;
  private int itemCount;
  private int inStockCount;
  private int onSaleCount;
  private boolean isEmpty;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static WishlistResponse fromWishlist(Wishlist wishlist) {
    List<WishlistItemResponse> itemResponses = wishlist.getItems().stream()
        .map(WishlistItemResponse::fromWishlistItem)
        .toList();

    return WishlistResponse.builder()
        .userId(wishlist.getUserId())
        .items(itemResponses)
        .itemCount(wishlist.getItemCount())
        .inStockCount(wishlist.getInStockItems().size())
        .onSaleCount(wishlist.getItemsOnSale().size())
        .isEmpty(wishlist.isEmpty())
        .createdAt(wishlist.getCreatedAt())
        .updatedAt(wishlist.getUpdatedAt())
        .build();
  }
}