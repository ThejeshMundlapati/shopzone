package com.shopzone.orderservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopzone.common.dto.response.*;
import com.shopzone.common.exception.*;
import com.shopzone.orderservice.client.*;
import com.shopzone.orderservice.config.OrderConfig;
import com.shopzone.orderservice.dto.request.CheckoutRequest;
import com.shopzone.orderservice.dto.response.*;
import com.shopzone.orderservice.model.*;
import com.shopzone.orderservice.model.enums.*;
import com.shopzone.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class CheckoutService {
    private final OrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderConfig orderConfig;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final CartClient cartClient;
    private final PaymentClient paymentClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderWithPaymentResponse placeOrderWithPayment(String userEmail, CheckoutRequest request) {
        log.info("Placing order with payment for user: {}", userEmail);

        // 1. Get user info from User Service (JWT gives us email, we need UUID)
        UserResponse user = userClient.getUserByEmail(userEmail);
        String actualUserId = user.getId();

        // 2. Get address from User Service
        Object addressObj = userClient.getAddress(actualUserId, request.getShippingAddressId());
        if (addressObj == null) throw new ResourceNotFoundException("Address not found");

        // Convert address map to snapshot
        @SuppressWarnings("unchecked")
        Map<String, Object> addrMap = objectMapper.convertValue(addressObj, Map.class);
        AddressSnapshot snapshot = AddressSnapshot.builder()
            .fullName((String) addrMap.get("fullName"))
            .phoneNumber((String) addrMap.get("phoneNumber"))
            .addressLine1((String) addrMap.get("addressLine1"))
            .addressLine2((String) addrMap.get("addressLine2"))
            .city((String) addrMap.get("city"))
            .state((String) addrMap.get("state"))
            .postalCode((String) addrMap.get("postalCode"))
            .country((String) addrMap.get("country"))
            .landmark((String) addrMap.get("landmark"))
            .build();

        // 3. Get cart items from Cart Service
        List<Map<String, Object>> cartItems = getCartItems(userEmail);
        if (cartItems == null || cartItems.isEmpty()) throw new BadRequestException("Cart is empty");

        // 4. Fetch all products and build order items
        List<String> productIds = cartItems.stream().map(i -> (String)i.get("productId")).collect(Collectors.toList());
        List<ProductResponse> products = productClient.getProductsByIds(productIds);
        Map<String, ProductResponse> productMap = products.stream().collect(Collectors.toMap(ProductResponse::getId, Function.identity()));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map<String, Object> cartItem : cartItems) {
            String productId = (String) cartItem.get("productId");
            int quantity = ((Number) cartItem.get("quantity")).intValue();
            ProductResponse product = productMap.get(productId);
            if (product == null) throw new BadRequestException("Product unavailable: " + cartItem.get("productName"));

            BigDecimal effectivePrice = product.getEffectivePrice();
            OrderItem orderItem = OrderItem.builder()
                .productId(product.getId()).productName(product.getName()).productSlug(product.getSlug())
                .productSku(product.getSku()).productImage(product.getFirstImage()).productBrand(product.getBrand())
                .unitPrice(product.getPrice()).discountPrice(product.getDiscountPrice())
                .effectivePrice(effectivePrice).quantity(quantity)
                .totalPrice(effectivePrice.multiply(BigDecimal.valueOf(quantity)))
                .build();
            orderItems.add(orderItem);
            subtotal = subtotal.add(orderItem.getTotalPrice());
        }

        // 5. Calculate totals
        BigDecimal taxRate = orderConfig.getTaxRate();
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        boolean freeShipping = subtotal.compareTo(orderConfig.getFreeShippingThreshold()) >= 0;
        BigDecimal shippingCost = freeShipping ? BigDecimal.ZERO : orderConfig.getFlatShippingRate();
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingCost);

        // 6. Create order
        Order order = Order.builder()
            .orderNumber(orderNumberGenerator.generate())
            .userId(actualUserId).userEmail(user.getEmail()).userFullName(user.getFullName())
            .shippingAddressId(request.getShippingAddressId()).shippingAddress(snapshot)
            .items(orderItems).subtotal(subtotal).taxRate(taxRate).taxAmount(taxAmount)
            .shippingCost(shippingCost).totalAmount(totalAmount)
            .status(OrderStatus.PENDING).paymentStatus(PaymentStatus.PENDING)
            .customerNotes(request.getCustomerNotes())
            .build();
        order = orderRepository.save(order);
        log.info("Order created: {}", order.getOrderNumber());

        // 7. Create payment intent via Payment Service
        Map<String, Object> payment = paymentClient.createPaymentIntent(
            order.getId(), order.getOrderNumber(), actualUserId, user.getEmail(), totalAmount);

        if (payment != null && payment.containsKey("paymentIntentId")) {
            order.setStripePaymentIntentId((String) payment.get("paymentIntentId"));
            order.setPaymentStatus(PaymentStatus.AWAITING_PAYMENT);
            orderRepository.save(order);
        }

        // 8. Clear cart
        cartClient.clearCart(userEmail);

        return OrderWithPaymentResponse.builder()
            .order(OrderResponse.fromEntity(order))
            .payment(payment)
            .build();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getCartItems(String userId) {
        try {
            org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
            String cartUrl = "http://localhost:8083/api/internal/cart/" + userId;
            var resp = rt.getForEntity(cartUrl, Map.class);
            if (resp.getBody() != null) {
                Map<String, Object> body = (Map<String, Object>) resp.getBody();
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data != null) return (List<Map<String, Object>>) data.get("items");
            }
            return null;
        } catch (Exception e) { log.error("Failed to get cart: {}", e.getMessage()); return null; }
    }
}