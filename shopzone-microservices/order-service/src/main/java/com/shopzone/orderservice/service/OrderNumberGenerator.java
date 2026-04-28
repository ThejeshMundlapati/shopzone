package com.shopzone.orderservice.service;

import com.shopzone.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component @RequiredArgsConstructor
public class OrderNumberGenerator {
    private final OrderRepository orderRepository;
    private final Random random = new Random();

    public String generate() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String orderNumber;
        do {
            String randomPart = String.valueOf((char)('A' + random.nextInt(26)))
                + String.valueOf((char)('A' + random.nextInt(26)))
                + String.valueOf((char)('0' + random.nextInt(10)))
                + String.valueOf((char)('A' + random.nextInt(26)));
            orderNumber = "ORD-" + datePart + "-" + randomPart;
        } while (orderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }
}
