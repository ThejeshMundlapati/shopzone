package com.shopzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist implements Serializable {

  private static final long serialVersionUID = 1L;

  private String userId;

  @Builder.Default
  private List<WishlistItem> items = new ArrayList<>();

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /**
   * Add item to wishlist
   */
  public boolean addItem(WishlistItem item) {
    if (containsProduct(item.getProductId())) {
      return false;
    }
    item.setAddedAt(LocalDateTime.now());
    items.add(item);
    this.updatedAt = LocalDateTime.now();
    return true;
  }

  /**
   * Remove item from wishlist
   */
  public boolean removeItem(String productId) {
    boolean removed = items.removeIf(item -> item.getProductId().equals(productId));
    if (removed) {
      this.updatedAt = LocalDateTime.now();
    }
    return removed;
  }

  /**
   * Check if product is in wishlist
   */
  public boolean containsProduct(String productId) {
    return items.stream()
        .anyMatch(item -> item.getProductId().equals(productId));
  }

  /**
   * Find item by product ID
   */
  public Optional<WishlistItem> findItemByProductId(String productId) {
    return items.stream()
        .filter(item -> item.getProductId().equals(productId))
        .findFirst();
  }

  /**
   * Clear all items
   */
  public void clear() {
    items.clear();
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Get count of items
   */
  @JsonIgnore
  public int getItemCount() {
    return items.size();
  }

  /**
   * Check if wishlist is empty
   */
  @JsonIgnore
  public boolean isEmpty() {
    return items.isEmpty();
  }

  /**
   * Get items that are in stock
   */
  @JsonIgnore
  public List<WishlistItem> getInStockItems() {
    return items.stream()
        .filter(WishlistItem::isInStock)
        .toList();
  }

  /**
   * Get items that are on sale
   */
  @JsonIgnore
  public List<WishlistItem> getItemsOnSale() {
    return items.stream()
        .filter(WishlistItem::hasDiscount)
        .toList();
  }
}