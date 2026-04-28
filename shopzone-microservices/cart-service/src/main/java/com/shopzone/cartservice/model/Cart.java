package com.shopzone.cartservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    @Builder.Default private List<CartItem> items = new ArrayList<>();
    private LocalDateTime createdAt, updatedAt;

    public void addItem(CartItem newItem) {
        Optional<CartItem> existing = findItemByProductId(newItem.getProductId());
        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + newItem.getQuantity());
            existing.get().setPrice(newItem.getPrice());
            existing.get().setDiscountPrice(newItem.getDiscountPrice());
        } else { items.add(newItem); }
    }
    public boolean removeItem(String productId) { return items.removeIf(i -> i.getProductId().equals(productId)); }
    public boolean updateItemQuantity(String productId, int qty) {
        return findItemByProductId(productId).map(i -> { i.setQuantity(qty); return true; }).orElse(false);
    }
    @JsonIgnore
    public Optional<CartItem> findItemByProductId(String productId) {
        return items.stream().filter(i -> i.getProductId().equals(productId)).findFirst();
    }
    public void clear() { items.clear(); }
    @JsonIgnore
    public boolean isEmpty() { return items == null || items.isEmpty(); }
    @JsonIgnore
    public int getUniqueItemCount() { return items != null ? items.size() : 0; }
    @JsonIgnore
    public BigDecimal getSubtotal() { return items.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
    @JsonIgnore
    public List<CartItem> getInvalidItems() { return items.stream().filter(i -> !i.isQuantityValid()).toList(); }
}