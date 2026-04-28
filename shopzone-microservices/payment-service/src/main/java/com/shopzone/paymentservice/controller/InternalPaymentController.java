package com.shopzone.paymentservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController @RequestMapping("/api/internal/payments") @RequiredArgsConstructor @Hidden
public class InternalPaymentController {
    private final PaymentService paymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createIntent(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = paymentService.createPaymentIntent(
            (String) body.get("orderId"), (String) body.get("orderNumber"),
            (String) body.get("userId"), (String) body.get("userEmail"),
            new BigDecimal(body.get("amount").toString()));
        return ResponseEntity.ok(ApiResponse.success("Intent created", result));
    }
}
