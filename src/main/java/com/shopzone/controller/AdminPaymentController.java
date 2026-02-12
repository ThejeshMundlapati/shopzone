package com.shopzone.controller;

import com.shopzone.dto.request.RefundRequest;
import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.response.PaymentResponse;
import com.shopzone.dto.response.RefundResponse;
import com.shopzone.model.enums.PaymentStatus;
import com.shopzone.service.PaymentService;
import com.shopzone.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Admin controller for payment management.
 */
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Payments", description = "Admin payment management APIs")
@SecurityRequirement(name = "bearerAuth")
public class AdminPaymentController {

  private final PaymentService paymentService;
  private final RefundService refundService;


  @GetMapping
  @Operation(summary = "Get all payments",
      description = "Get paginated list of all payments (admin only)")
  public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getAllPayments(
      @RequestParam(required = false) PaymentStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<PaymentResponse> payments = paymentService.getAllPayments(status, pageable);
    return ResponseEntity.ok(ApiResponse.success("Payments retrieved", payments));
  }


  @GetMapping("/{orderNumber}")
  @Operation(summary = "Get payment details",
      description = "Get detailed payment information for an order (admin only)")
  public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentDetails(
      @PathVariable String orderNumber) {

    PaymentResponse payment = paymentService.getPaymentStatus(orderNumber, null);
    return ResponseEntity.ok(ApiResponse.success("Payment details retrieved", payment));
  }


  @PostMapping("/refund")
  @Operation(summary = "Process refund",
      description = "Process a full or partial refund for an order (admin only)")
  public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
      @Valid @RequestBody RefundRequest request) {

    log.info("Admin processing refund for order: {}", request.getOrderNumber());

    RefundResponse response = refundService.processRefund(request);

    String message = request.isFullRefund()
        ? "Full refund processed successfully"
        : "Partial refund of $" + request.getAmount() + " processed successfully";

    return ResponseEntity.ok(ApiResponse.success(message, response));
  }


  @GetMapping("/{orderNumber}/refund-eligibility")
  @Operation(summary = "Check refund eligibility",
      description = "Check if an order is eligible for refund (admin only)")
  public ResponseEntity<ApiResponse<RefundService.RefundEligibility>> checkRefundEligibility(
      @PathVariable String orderNumber) {

    RefundService.RefundEligibility eligibility = refundService.checkRefundEligibility(orderNumber);
    return ResponseEntity.ok(ApiResponse.success("Refund eligibility checked", eligibility));
  }


  @GetMapping("/stats")
  @Operation(summary = "Get payment statistics",
      description = "Get payment statistics for dashboard (admin only)")
  public ResponseEntity<ApiResponse<PaymentStats>> getPaymentStats() {

    PaymentStats stats = paymentService.getPaymentStatistics();

    return ResponseEntity.ok(ApiResponse.success("Payment statistics retrieved", stats));
  }

  /**
   * Payment statistics DTO.
   */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class PaymentStats {
    private long totalPayments;
    private long successfulPayments;
    private long failedPayments;
    private BigDecimal totalRevenue;
    private BigDecimal totalRefunded;
  }
}