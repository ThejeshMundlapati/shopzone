package com.shopzone.service;

import com.shopzone.config.OrderConfig;
import com.shopzone.config.StripeConfig;
import com.shopzone.dto.request.CheckoutRequest;
import com.shopzone.dto.response.*;
import com.shopzone.exception.BadRequestException;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.*;
import com.shopzone.model.enums.OrderStatus;
import com.shopzone.model.enums.PaymentStatus;
import com.shopzone.repository.jpa.OrderRepository;
import com.shopzone.repository.jpa.AddressRepository;
import com.shopzone.repository.jpa.UserRepository;
import com.shopzone.repository.mongo.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service handling checkout validation, calculation, and order placement.
 */
@Service
@Slf4j
public class CheckoutService {

  private final CartService cartService;
  private final ProductRepository productRepository;
  private final AddressRepository addressRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final OrderNumberGenerator orderNumberGenerator;
  private final OrderConfig orderConfig;
  private final StripeConfig stripeConfig;

  private PaymentService paymentService;

  @Autowired
  public CheckoutService(CartService cartService,
                         ProductRepository productRepository,
                         AddressRepository addressRepository,
                         UserRepository userRepository,
                         OrderRepository orderRepository,
                         OrderNumberGenerator orderNumberGenerator,
                         OrderConfig orderConfig,
                         StripeConfig stripeConfig) {
    this.cartService = cartService;
    this.productRepository = productRepository;
    this.addressRepository = addressRepository;
    this.userRepository = userRepository;
    this.orderRepository = orderRepository;
    this.orderNumberGenerator = orderNumberGenerator;
    this.orderConfig = orderConfig;
    this.stripeConfig = stripeConfig;
  }

  @Autowired
  public void setPaymentService(@Lazy PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  /**
   * Validate cart for checkout.
   */
  @Transactional(readOnly = true)
  public CheckoutValidationResponse validateCart(String userId) {
    log.info("Validating cart for user: {}", userId);

    Cart cart = cartService.getCartEntity(userId);
    if (cart == null || cart.getItems().isEmpty()) {
      return CheckoutValidationResponse.emptyCart();
    }

    List<CartValidationIssue> errors = new ArrayList<>();
    List<CartValidationIssue> warnings = new ArrayList<>();

    List<String> productIds = cart.getItems().stream()
        .map(CartItem::getProductId)
        .collect(Collectors.toList());

    Map<String, Product> productMap = productRepository.findByIdIn(productIds)
        .stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    for (CartItem item : cart.getItems()) {
      Product product = productMap.get(item.getProductId());

      if (product == null) {
        errors.add(CartValidationIssue.productNotFound(
            item.getProductId(), item.getProductName()));
        continue;
      }

      if (!product.isActive()) {
        errors.add(CartValidationIssue.productUnavailable(
            product.getId(), product.getName()));
        continue;
      }

      if (product.getStock() == null || product.getStock() == 0) {
        errors.add(CartValidationIssue.outOfStock(
            product.getId(), product.getName()));
        continue;
      }

      if (product.getStock() < item.getQuantity()) {
        errors.add(CartValidationIssue.insufficientStock(
            product.getId(),
            product.getName(),
            product.getStock(),
            item.getQuantity()));
        continue;
      }

      BigDecimal currentEffectivePrice = getEffectivePrice(product);
      BigDecimal cartEffectivePrice = item.getEffectivePrice();
      if (currentEffectivePrice != null && cartEffectivePrice != null &&
          cartEffectivePrice.compareTo(currentEffectivePrice) != 0) {
        warnings.add(CartValidationIssue.priceChanged(
            product.getId(),
            product.getName(),
            cartEffectivePrice,
            currentEffectivePrice));
      }
    }

    CartResponse cartResponse = cartService.getCart(userId);

    if (!errors.isEmpty()) {
      return CheckoutValidationResponse.invalid(errors, warnings, cartResponse);
    }

    if (!warnings.isEmpty()) {
      return CheckoutValidationResponse.validWithWarnings(cartResponse, warnings);
    }

    return CheckoutValidationResponse.valid(cartResponse);
  }

  /**
   * Calculate checkout totals (preview).
   */
  @Transactional(readOnly = true)
  public CheckoutPreviewResponse calculateTotals(String userId, String addressId) {
    log.info("Calculating checkout totals for user: {}, address: {}", userId, addressId);

    CheckoutValidationResponse validation = validateCart(userId);
    if (!validation.isValid()) {
      throw new BadRequestException("Cart has validation errors. Please resolve them first.");
    }

    Address address = addressRepository.findByIdAndUserIdAndActiveTrue(addressId, userId)
        .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

    CartResponse cart = validation.getCart();

    BigDecimal subtotal = cart.getSubtotal();
    BigDecimal itemSavings = cart.getTotalSavings() != null ? cart.getTotalSavings() : BigDecimal.ZERO;
    BigDecimal taxRate = orderConfig.getTaxRate();
    BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);

    BigDecimal freeShippingThreshold = orderConfig.getFreeShippingThreshold();
    boolean freeShipping = subtotal.compareTo(freeShippingThreshold) >= 0;
    BigDecimal shippingCost = freeShipping ? BigDecimal.ZERO : orderConfig.getFlatShippingRate();

    BigDecimal amountToFreeShipping = null;
    if (!freeShipping) {
      amountToFreeShipping = freeShippingThreshold.subtract(subtotal);
    }

    BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingCost);

    return CheckoutPreviewResponse.builder()
        .cart(cart)
        .shippingAddress(AddressResponse.fromAddress(address))
        .subtotal(subtotal)
        .itemSavings(itemSavings)
        .taxRate(taxRate.multiply(BigDecimal.valueOf(100)))
        .taxAmount(taxAmount)
        .shippingCost(shippingCost)
        .freeShipping(freeShipping)
        .freeShippingThreshold(freeShippingThreshold)
        .amountToFreeShipping(amountToFreeShipping)
        .totalAmount(totalAmount)
        .totalItems(cart.getTotalItems())
        .uniqueProducts(cart.getUniqueItemCount())
        .build();
  }

  /**
   * Place an order from the user's cart (Original Week 4 method - unchanged).
   * Stock is reduced immediately. Use placeOrderWithPayment for Stripe.
   */
  @Transactional
  public OrderResponse placeOrder(String userId, CheckoutRequest request) {
    log.info("Placing order for user: {}", userId);

    CheckoutValidationResponse validation = validateCart(userId);
    if (!validation.isValid()) {
      throw new BadRequestException("Cart has validation errors: " +
          validation.getErrors().stream()
              .map(CartValidationIssue::getMessage)
              .collect(Collectors.joining(", ")));
    }

    Address address = addressRepository.findByIdAndUserIdAndActiveTrue(
            request.getShippingAddressId(), userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Shipping address not found or doesn't belong to user"));

    UUID userUUID = UUID.fromString(userId);
    User user = userRepository.findById(userUUID)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Cart cart = cartService.getCartEntity(userId);

    List<String> productIds = cart.getItems().stream()
        .map(CartItem::getProductId)
        .collect(Collectors.toList());
    Map<String, Product> productMap = productRepository.findByIdIn(productIds)
        .stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal subtotal = BigDecimal.ZERO;

    for (CartItem cartItem : cart.getItems()) {
      Product product = productMap.get(cartItem.getProductId());
      if (product == null) {
        throw new BadRequestException("Product no longer available: " + cartItem.getProductName());
      }

      OrderItem orderItem = OrderItem.fromCartItem(cartItem, product);
      orderItems.add(orderItem);
      subtotal = subtotal.add(orderItem.getTotalPrice());
    }

    BigDecimal taxRate = orderConfig.getTaxRate();
    BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
    BigDecimal freeShippingThreshold = orderConfig.getFreeShippingThreshold();
    boolean freeShipping = subtotal.compareTo(freeShippingThreshold) >= 0;
    BigDecimal shippingCost = freeShipping ? BigDecimal.ZERO : orderConfig.getFlatShippingRate();
    BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingCost);

    String orderNumber = orderNumberGenerator.generate();

    Order order = Order.builder()
        .orderNumber(orderNumber)
        .userId(userId)
        .userEmail(user.getEmail())
        .userFullName(user.getFullName())
        .shippingAddressId(address.getId())
        .shippingAddress(AddressSnapshot.fromAddress(address))
        .items(orderItems)
        .subtotal(subtotal)
        .taxRate(taxRate)
        .taxAmount(taxAmount)
        .shippingCost(shippingCost)
        .totalAmount(totalAmount)
        .status(OrderStatus.PENDING)
        .paymentStatus(PaymentStatus.PENDING)
        .customerNotes(request.getCustomerNotes())
        .build();

    order = orderRepository.save(order);
    log.info("Order created: {}", orderNumber);

    for (CartItem cartItem : cart.getItems()) {
      int result = productRepository.reduceStock(
          cartItem.getProductId(),
          cartItem.getQuantity(),
          -cartItem.getQuantity()
      );

      if (result == 0) {
        log.error("Failed to reduce stock for product: {}", cartItem.getProductId());
        throw new BadRequestException(
            "Unable to reserve stock for: " + cartItem.getProductName() +
                ". Please try again.");
      }
      log.debug("Reduced stock for product {} by {}", cartItem.getProductId(), cartItem.getQuantity());
    }

    cartService.clearCart(userId);
    log.info("Cart cleared for user: {}", userId);

    return OrderResponse.fromEntity(order);
  }


  /**
   * Place an order with Stripe payment integration.
   * Stock is NOT reduced here - it's reduced when payment succeeds via webhook.
   */
  @Transactional
  public OrderWithPaymentResponse placeOrderWithPayment(String userId, CheckoutRequest request) {
    log.info("Placing order with payment for user: {}", userId);

    CheckoutValidationResponse validation = validateCart(userId);
    if (!validation.isValid()) {
      throw new BadRequestException("Cart has validation errors: " +
          validation.getErrors().stream()
              .map(CartValidationIssue::getMessage)
              .collect(Collectors.joining(", ")));
    }

    Address address = addressRepository.findByIdAndUserIdAndActiveTrue(
            request.getShippingAddressId(), userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Shipping address not found or doesn't belong to user"));

    UUID userUUID = UUID.fromString(userId);
    User user = userRepository.findById(userUUID)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Cart cart = cartService.getCartEntity(userId);

    List<String> productIds = cart.getItems().stream()
        .map(CartItem::getProductId)
        .collect(Collectors.toList());
    Map<String, Product> productMap = productRepository.findByIdIn(productIds)
        .stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal subtotal = BigDecimal.ZERO;

    for (CartItem cartItem : cart.getItems()) {
      Product product = productMap.get(cartItem.getProductId());
      if (product == null) {
        throw new BadRequestException("Product no longer available: " + cartItem.getProductName());
      }

      OrderItem orderItem = OrderItem.fromCartItem(cartItem, product);
      orderItems.add(orderItem);
      subtotal = subtotal.add(orderItem.getTotalPrice());
    }

    BigDecimal taxRate = orderConfig.getTaxRate();
    BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
    BigDecimal freeShippingThreshold = orderConfig.getFreeShippingThreshold();
    boolean freeShipping = subtotal.compareTo(freeShippingThreshold) >= 0;
    BigDecimal shippingCost = freeShipping ? BigDecimal.ZERO : orderConfig.getFlatShippingRate();
    BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingCost);

    String orderNumber = orderNumberGenerator.generate();

    Order order = Order.builder()
        .orderNumber(orderNumber)
        .userId(userId)
        .userEmail(user.getEmail())
        .userFullName(user.getFullName())
        .shippingAddressId(address.getId())
        .shippingAddress(AddressSnapshot.fromAddress(address))
        .items(orderItems)
        .subtotal(subtotal)
        .taxRate(taxRate)
        .taxAmount(taxAmount)
        .shippingCost(shippingCost)
        .totalAmount(totalAmount)
        .status(OrderStatus.PENDING)
        .paymentStatus(PaymentStatus.PENDING)
        .customerNotes(request.getCustomerNotes())
        .build();

    order = orderRepository.save(order);
    log.info("Order created (pending payment): {}", orderNumber);

    PaymentIntentResponse paymentIntent = paymentService.createPaymentIntent(
        order.getOrderNumber(),
        userId
    );

    cartService.clearCart(userId);
    log.info("Cart cleared for user: {}", userId);


    return OrderWithPaymentResponse.builder()
        .order(OrderResponse.fromEntity(order))
        .payment(paymentIntent)
        .build();
  }


  /**
   * Get effective price from Product (handles null discountPrice).
   */
  private BigDecimal getEffectivePrice(Product product) {
    if (product.getDiscountPrice() != null &&
        product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0) {
      return product.getDiscountPrice();
    }
    return product.getPrice();
  }
}