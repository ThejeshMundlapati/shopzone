package com.shopzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {

  private static final long serialVersionUID = 1L;

  private String userId;

  @Builder.Default
  private List<CartItem> items = new ArrayList<>();

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /**
   * Add item to cart or update quantity if exists
   */
  public void addItem(CartItem newItem) {
    Optional<CartItem> existingItem = findItemByProductId(newItem.getProductId());

    if (existingItem.isPresent()) {
      CartItem item = existingItem.get();
      item.setQuantity(item.getQuantity() + newItem.getQuantity());
      item.setPrice(newItem.getPrice());
      item.setDiscountPrice(newItem.getDiscountPrice());
      item.setAvailableStock(newItem.getAvailableStock());
      item.setImageUrl(newItem.getImageUrl());
    } else {
      newItem.setAddedAt(LocalDateTime.now());
      items.add(newItem);
    }
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Update item quantity
   */
  public boolean updateItemQuantity(String productId, int quantity) {
    Optional<CartItem> item = findItemByProductId(productId);
    if (item.isPresent()) {
      item.get().setQuantity(quantity);
      this.updatedAt = LocalDateTime.now();
      return true;
    }
    return false;
  }

  /**
   * Remove item from cart
   */
  public boolean removeItem(String productId) {
    boolean removed = items.removeIf(item -> item.getProductId().equals(productId));
    if (removed) {
      this.updatedAt = LocalDateTime.now();
    }
    return removed;
  }

  /**
   * Clear all items from cart
   */
  public void clear() {
    items.clear();
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Find item by product ID
   */
  public Optional<CartItem> findItemByProductId(String productId) {
    return items.stream()
        .filter(item -> item.getProductId().equals(productId))
        .findFirst();
  }

  /**
   * Calculate subtotal (before tax/shipping)
   */
  @JsonIgnore
  public BigDecimal getSubtotal() {
    return items.stream()
        .map(CartItem::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Calculate total savings from discounts
   */
  @JsonIgnore
  public BigDecimal getTotalSavings() {
    return items.stream()
        .map(CartItem::getSavings)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Get total number of items
   */
  @JsonIgnore
  public int getTotalItems() {
    return items.stream()
        .mapToInt(CartItem::getQuantity)
        .sum();
  }

  /**
   * Get count of unique products
   */
  @JsonIgnore
  public int getUniqueItemCount() {
    return items.size();
  }

  /**
   * Check if cart is empty
   */
  @JsonIgnore
  public boolean isEmpty() {
    return items.isEmpty();
  }

  /**
   * Validate all items have valid quantities
   */
  @JsonIgnore
  public List<CartItem> getInvalidItems() {
    return items.stream()
        .filter(item -> !item.isQuantityValid())
        .toList();
  }
}