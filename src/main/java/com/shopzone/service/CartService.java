package com.shopzone.service;

import com.shopzone.dto.request.AddToCartRequest;
import com.shopzone.dto.request.UpdateCartItemRequest;
import com.shopzone.dto.response.CartResponse;
import com.shopzone.exception.BadRequestException;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.Cart;
import com.shopzone.model.CartItem;
import com.shopzone.model.Product;
import com.shopzone.repository.CartRepository;
import com.shopzone.repository.mongo.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;

  @Value("${cart.max-items:50}")
  private int maxCartItems;

  @Value("${cart.max-quantity-per-item:10}")
  private int maxQuantityPerItem;

  /**
   * Get user's cart
   */
  public CartResponse getCart(String userId) {
    Cart cart = cartRepository.getOrCreateCart(userId);
    refreshCartItemsData(cart);
    return CartResponse.fromCart(cart);
  }

  /**
   * Add item to cart
   */
  public CartResponse addToCart(String userId, AddToCartRequest request) {
    Product product = productRepository.findByIdAndActiveTrue(request.getProductId())
        .orElseThrow(() -> new ResourceNotFoundException("Product not found or inactive"));

    if (product.getStock() < request.getQuantity()) {
      throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
    }

    if (request.getQuantity() > maxQuantityPerItem) {
      throw new BadRequestException("Maximum quantity per item is " + maxQuantityPerItem);
    }

    Cart cart = cartRepository.getOrCreateCart(userId);

    if (cart.getUniqueItemCount() >= maxCartItems &&
        cart.findItemByProductId(request.getProductId()).isEmpty()) {
      throw new BadRequestException("Cart cannot have more than " + maxCartItems + " unique items");
    }

    CartItem cartItem = CartItem.builder()
        .productId(product.getId())
        .productName(product.getName())
        .productSlug(product.getSlug())
        .price(product.getPrice())
        .discountPrice(product.getDiscountPrice())
        .quantity(request.getQuantity())
        .imageUrl(getFirstImage(product))
        .availableStock(product.getStock())
        .addedAt(LocalDateTime.now())
        .build();

    cart.findItemByProductId(request.getProductId()).ifPresent(existing -> {
      int newQuantity = existing.getQuantity() + request.getQuantity();
      if (newQuantity > maxQuantityPerItem) {
        throw new BadRequestException("Maximum quantity per item is " + maxQuantityPerItem);
      }
      if (newQuantity > product.getStock()) {
        throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
      }
    });

    cart.addItem(cartItem);
    cartRepository.save(cart);

    log.info("Added product {} to cart for user {}", product.getId(), userId);
    return CartResponse.fromCart(cart);
  }

  /**
   * Update cart item quantity
   */
  public CartResponse updateCartItem(String userId, UpdateCartItemRequest request) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

    Product product = productRepository.findByIdAndActiveTrue(request.getProductId())
        .orElseThrow(() -> new ResourceNotFoundException("Product not found or inactive"));

    if (request.getQuantity() > maxQuantityPerItem) {
      throw new BadRequestException("Maximum quantity per item is " + maxQuantityPerItem);
    }

    if (request.getQuantity() > product.getStock()) {
      throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
    }

    boolean updated = cart.updateItemQuantity(request.getProductId(), request.getQuantity());
    if (!updated) {
      throw new ResourceNotFoundException("Product not found in cart");
    }

    cart.findItemByProductId(request.getProductId()).ifPresent(item -> {
      item.setPrice(product.getPrice());
      item.setDiscountPrice(product.getDiscountPrice());
      item.setAvailableStock(product.getStock());
      item.setImageUrl(getFirstImage(product));
    });

    cartRepository.save(cart);

    log.info("Updated quantity for product {} in cart for user {}", request.getProductId(), userId);
    return CartResponse.fromCart(cart);
  }

  /**
   * Remove item from cart
   */
  public CartResponse removeFromCart(String userId, String productId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

    boolean removed = cart.removeItem(productId);
    if (!removed) {
      throw new ResourceNotFoundException("Product not found in cart");
    }

    cartRepository.save(cart);

    log.info("Removed product {} from cart for user {}", productId, userId);
    return CartResponse.fromCart(cart);
  }

  /**
   * Clear cart
   */
  public void clearCart(String userId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

    cart.clear();
    cartRepository.save(cart);

    log.info("Cleared cart for user {}", userId);
  }

  /**
   * Refresh cart items with latest product data
   */
  private void refreshCartItemsData(Cart cart) {
    boolean needsSave = false;

    for (CartItem item : cart.getItems()) {
      productRepository.findByIdAndActiveTrue(item.getProductId()).ifPresentOrElse(
          product -> {
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setDiscountPrice(product.getDiscountPrice());
            item.setAvailableStock(product.getStock());
            item.setImageUrl(getFirstImage(product));
          },
          () -> {
            item.setAvailableStock(0);
          }
      );
    }

    List<CartItem> invalidItems = cart.getItems().stream()
        .filter(item -> item.getAvailableStock() == 0)
        .toList();

    if (!invalidItems.isEmpty()) {
      invalidItems.forEach(item -> cart.removeItem(item.getProductId()));
      needsSave = true;
    }

    if (needsSave) {
      cartRepository.save(cart);
    }
  }

  /**
   * Get first image URL from product
   */
  private String getFirstImage(Product product) {
    if (product.getImages() != null && !product.getImages().isEmpty()) {
      return product.getImages().get(0);
    }
    return null;
  }

  /**
   * Validate cart for checkout
   */
  public CartResponse validateCartForCheckout(String userId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

    if (cart.isEmpty()) {
      throw new BadRequestException("Cart is empty");
    }

    refreshCartItemsData(cart);

    List<CartItem> invalidItems = cart.getInvalidItems();
    if (!invalidItems.isEmpty()) {
      throw new BadRequestException("Some items in cart have stock issues. Please update quantities.");
    }

    return CartResponse.fromCart(cart);
  }
}