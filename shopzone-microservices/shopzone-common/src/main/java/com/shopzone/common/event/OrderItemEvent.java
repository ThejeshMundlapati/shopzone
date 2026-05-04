package com.shopzone.common.event;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents a single line item in an order event.
 * Nested inside OrderEvent to carry product details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
