package com.shopzone.service;

import com.shopzone.dto.request.AddToCartRequest;
import com.shopzone.dto.response.CartResponse;
import com.shopzone.dto.response.WishlistResponse;
import com.shopzone.exception.BadRequestException;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.Product;
import com.shopzone.model.Wishlist;
import com.shopzone.model.WishlistItem;
import com.shopzone.repository.mongo.ProductRepository;
import com.shopzone.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

  private final WishlistRepository wishlistRepository;
  private final ProductRepository productRepository;
  private final CartService cartService;

  /**
   * Get user's wishlist
   */
  public WishlistResponse getWishlist(String userId) {
    Wishlist wishlist = wishlistRepository.getOrCreateWishlist(userId);
    refreshWishlistItemsData(wishlist);
    return WishlistResponse.fromWishlist(wishlist);
  }

  /**
   * Add product to wishlist
   */
  public WishlistResponse addToWishlist(String userId, String productId) {
    Product product = productRepository.findByIdAndActiveTrue(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found or inactive"));

    Wishlist wishlist = wishlistRepository.getOrCreateWishlist(userId);

    if (wishlist.containsProduct(productId)) {
      throw new BadRequestException("Product already in wishlist");
    }

    WishlistItem item = WishlistItem.builder()
        .productId(product.getId())
        .productName(product.getName())
        .productSlug(product.getSlug())
        .price(product.getPrice())
        .discountPrice(product.getDiscountPrice())
        .imageUrl(getFirstImage(product))
        .inStock(product.getStock() > 0)
        .build();

    wishlist.addItem(item);
    wishlistRepository.save(wishlist);

    log.info("Added product {} to wishlist for user {}", productId, userId);
    return WishlistResponse.fromWishlist(wishlist);
  }

  /**
   * Remove product from wishlist
   */
  public WishlistResponse removeFromWishlist(String userId, String productId) {
    Wishlist wishlist = wishlistRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));

    boolean removed = wishlist.removeItem(productId);
    if (!removed) {
      throw new ResourceNotFoundException("Product not found in wishlist");
    }

    wishlistRepository.save(wishlist);

    log.info("Removed product {} from wishlist for user {}", productId, userId);
    return WishlistResponse.fromWishlist(wishlist);
  }

  /**
   * Move item from wishlist to cart
   */
  public CartResponse moveToCart(String userId, String productId) {
    Wishlist wishlist = wishlistRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));

    if (!wishlist.containsProduct(productId)) {
      throw new ResourceNotFoundException("Product not found in wishlist");
    }

    AddToCartRequest cartRequest = AddToCartRequest.builder()
        .productId(productId)
        .quantity(1)
        .build();

    CartResponse cartResponse = cartService.addToCart(userId, cartRequest);

    wishlist.removeItem(productId);
    wishlistRepository.save(wishlist);

    log.info("Moved product {} from wishlist to cart for user {}", productId, userId);
    return cartResponse;
  }

  /**
   * Move all wishlist items to cart
   */
  public CartResponse moveAllToCart(String userId) {
    Wishlist wishlist = wishlistRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));

    if (wishlist.isEmpty()) {
      throw new BadRequestException("Wishlist is empty");
    }

    CartResponse lastCartResponse = null;

    for (WishlistItem item : wishlist.getInStockItems()) {
      try {
        AddToCartRequest cartRequest = AddToCartRequest.builder()
            .productId(item.getProductId())
            .quantity(1)
            .build();
        lastCartResponse = cartService.addToCart(userId, cartRequest);
        wishlist.removeItem(item.getProductId());
      } catch (Exception e) {
        log.warn("Could not move product {} to cart: {}", item.getProductId(), e.getMessage());
      }
    }

    wishlistRepository.save(wishlist);

    log.info("Moved {} items from wishlist to cart for user {}",
        wishlist.getItemCount(), userId);

    return lastCartResponse != null ? lastCartResponse : cartService.getCart(userId);
  }

  /**
   * Clear wishlist
   */
  public void clearWishlist(String userId) {
    Wishlist wishlist = wishlistRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));

    wishlist.clear();
    wishlistRepository.save(wishlist);

    log.info("Cleared wishlist for user {}", userId);
  }

  /**
   * Check if product is in wishlist
   */
  public boolean isInWishlist(String userId, String productId) {
    return wishlistRepository.findByUserId(userId)
        .map(wishlist -> wishlist.containsProduct(productId))
        .orElse(false);
  }

  /**
   * Refresh wishlist items with latest product data
   */
  private void refreshWishlistItemsData(Wishlist wishlist) {
    boolean needsSave = false;

    for (WishlistItem item : wishlist.getItems()) {
      productRepository.findByIdAndActiveTrue(item.getProductId()).ifPresentOrElse(
          product -> {
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setDiscountPrice(product.getDiscountPrice());
            item.setImageUrl(getFirstImage(product));
            item.setInStock(product.getStock() > 0);
          },
          () -> {
            item.setInStock(false);
          }
      );
    }

    if (needsSave) {
      wishlistRepository.save(wishlist);
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
}