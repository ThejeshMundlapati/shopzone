package com.shopzone.controller;

import com.shopzone.dto.request.CreatePaymentRequest;
import com.shopzone.dto.response.ApiResponse;
import com.shopzone.dto.response.PaymentIntentResponse;
import com.shopzone.dto.response.PaymentResponse;
import com.shopzone.model.User;
import com.shopzone.service.PaymentService;
import com.shopzone.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for payment operations.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment processing APIs")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

  private final PaymentService paymentService;
  private final RefundService refundService;


  @PostMapping("/create-intent")
  @Operation(summary = "Create payment intent",
      description = "Create a Stripe payment intent for an existing order")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment intent created"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid order or already paid"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
  })
  public ResponseEntity<ApiResponse<PaymentIntentResponse>> createPaymentIntent(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody CreatePaymentRequest request) {

    log.info("Creating payment intent for order: {}", request.getOrderNumber());

    PaymentIntentResponse response = paymentService.createPaymentIntent(
        request.getOrderNumber(),
        user.getId().toString()
    );

    return ResponseEntity.ok(ApiResponse.success(
        "Payment intent created. Use clientSecret with Stripe.js to complete payment.",
        response
    ));
  }


  @GetMapping("/{orderNumber}")
  @Operation(summary = "Get payment status",
      description = "Get payment details for an order")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment details retrieved"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
  })
  public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(
      @AuthenticationPrincipal User user,
      @PathVariable String orderNumber) {

    PaymentResponse payment = paymentService.getPaymentStatus(
        orderNumber,
        user.getId().toString()
    );

    return ResponseEntity.ok(ApiResponse.success("Payment status retrieved", payment));
  }


  @GetMapping("/history")
  @Operation(summary = "Get payment history",
      description = "Get paginated list of user's payments")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment history retrieved"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
  })
  public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPaymentHistory(
      @AuthenticationPrincipal User user,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    Set<String> allowedSortFields = Set.of("createdAt", "amount", "status", "paidAt");
    if (!allowedSortFields.contains(sortBy)) {
      sortBy = "createdAt";
    }

    Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<PaymentResponse> payments = paymentService.getUserPayments(
        user.getId().toString(),
        pageable
    );

    return ResponseEntity.ok(ApiResponse.success("Payment history retrieved", payments));
  }


  @GetMapping("/{orderNumber}/refund-eligibility")
  @Operation(summary = "Check refund eligibility",
      description = "Check if an order is eligible for refund")
  public ResponseEntity<ApiResponse<RefundService.RefundEligibility>> checkRefundEligibility(
      @AuthenticationPrincipal User user,
      @PathVariable String orderNumber) {

    RefundService.RefundEligibility eligibility = refundService.checkRefundEligibility(orderNumber);
    return ResponseEntity.ok(ApiResponse.success("Refund eligibility checked", eligibility));
  }
}