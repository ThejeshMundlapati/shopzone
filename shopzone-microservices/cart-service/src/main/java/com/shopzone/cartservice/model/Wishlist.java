package com.shopzone.cartservice.model;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Wishlist implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    @Builder.Default private List<WishlistItem> items = new ArrayList<>();
    private LocalDateTime createdAt, updatedAt;

    public void addItem(WishlistItem item) { items.add(item); }
    public boolean removeItem(String productId) { return items.removeIf(i -> i.getProductId().equals(productId)); }
    public boolean containsProduct(String productId) { return items.stream().anyMatch(i -> i.getProductId().equals(productId)); }
    public void clear() { items.clear(); }
    public boolean isEmpty() { return items == null || items.isEmpty(); }
    public int getItemCount() { return items != null ? items.size() : 0; }
    public List<WishlistItem> getInStockItems() { return items.stream().filter(WishlistItem::isInStock).toList(); }
}
