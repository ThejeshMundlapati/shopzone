package com.shopzone.paymentservice.controller;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.paymentservice.model.enums.PaymentStatus;
import com.shopzone.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/admin/payments") @RequiredArgsConstructor
@Tag(name = "Admin Payments", description = "Admin payment management")
public class AdminPaymentController {
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getAll(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(ApiResponse.success("Payments", paymentService.getAllPayments(status, PageRequest.of(page, size, sort))));
    }
}
