package com.shopzone.cartservice.service;

import com.shopzone.cartservice.client.ProductClient;
import com.shopzone.cartservice.dto.request.AddToCartRequest;
import com.shopzone.cartservice.dto.request.UpdateCartItemRequest;
import com.shopzone.cartservice.dto.response.CartResponse;
import com.shopzone.cartservice.model.Cart;
import com.shopzone.cartservice.model.CartItem;
import com.shopzone.cartservice.repository.CartRepository;
import com.shopzone.common.dto.response.ProductResponse;
import com.shopzone.common.exception.BadRequestException;
import com.shopzone.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor @Slf4j
public class CartService {
    private final CartRepository cartRepository;
    private final ProductClient productClient;
    @Value("${cart.max-items:50}") private int maxItems;
    @Value("${cart.max-quantity-per-item:10}") private int maxQty;

    public CartResponse getCart(String userId) {
        Cart cart = cartRepository.getOrCreateCart(userId);
        refreshCartItems(cart);
        return CartResponse.fromCart(cart);
    }

    public Cart getCartEntity(String userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null || cart.isEmpty()) return null;
        refreshCartItems(cart);
        return cart;
    }

    public CartResponse addToCart(String userId, AddToCartRequest request) {
        ProductResponse product = productClient.getProduct(request.getProductId());
        if (product.getStock() < request.getQuantity()) throw new BadRequestException("Insufficient stock");
        if (request.getQuantity() > maxQty) throw new BadRequestException("Max quantity is " + maxQty);

        Cart cart = cartRepository.getOrCreateCart(userId);
        if (cart.getUniqueItemCount() >= maxItems && cart.findItemByProductId(request.getProductId()).isEmpty())
            throw new BadRequestException("Cart full");

        CartItem item = CartItem.builder().productId(product.getId()).productName(product.getName())
            .productSlug(product.getSlug()).price(product.getPrice()).discountPrice(product.getDiscountPrice())
            .quantity(request.getQuantity()).imageUrl(product.getFirstImage())
            .availableStock(product.getStock()).addedAt(LocalDateTime.now()).build();

        cart.addItem(item);
        cartRepository.save(cart);
        return CartResponse.fromCart(cart);
    }

    public CartResponse updateCartItem(String userId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        ProductResponse product = productClient.getProduct(request.getProductId());
        if (request.getQuantity() > product.getStock()) throw new BadRequestException("Insufficient stock");
        if (!cart.updateItemQuantity(request.getProductId(), request.getQuantity()))
            throw new ResourceNotFoundException("Product not in cart");
        cartRepository.save(cart);
        return CartResponse.fromCart(cart);
    }

    public CartResponse removeFromCart(String userId, String productId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        if (!cart.removeItem(productId)) throw new ResourceNotFoundException("Product not in cart");
        cartRepository.save(cart);
        return CartResponse.fromCart(cart);
    }

    public void clearCart(String userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> { cart.clear(); cartRepository.save(cart); });
    }

    private void refreshCartItems(Cart cart) {
        for (CartItem item : cart.getItems()) {
            try {
                ProductResponse p = productClient.getProduct(item.getProductId());
                item.setProductName(p.getName()); item.setPrice(p.getPrice());
                item.setDiscountPrice(p.getDiscountPrice()); item.setAvailableStock(p.getStock());
                item.setImageUrl(p.getFirstImage());
            } catch (Exception e) { item.setAvailableStock(0); }
        }
        cart.getItems().removeIf(i -> i.getAvailableStock() == 0);
    }
}
