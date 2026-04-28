package com.shopzone.paymentservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.paymentservice.model.enums.PaymentStatus;
import com.shopzone.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Set;

@RestController @RequestMapping("/api/payments") @RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment APIs")
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus(Authentication auth, @PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.success("Payment status", paymentService.getPaymentStatus(orderNumber)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> history(
            Authentication auth, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(defaultValue = "desc") String sortDir) {
        if (!Set.of("createdAt","amount","status","paidAt").contains(sortBy)) sortBy = "createdAt";
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        // JWT principal is email — payment history is stored by userId (UUID from order flow)
        // For now, pass the email and let PaymentService handle the lookup
        return ResponseEntity.ok(ApiResponse.success("History",
            paymentService.getUserPayments((String) auth.getPrincipal(), PageRequest.of(page, size, sort))));
    }
}
