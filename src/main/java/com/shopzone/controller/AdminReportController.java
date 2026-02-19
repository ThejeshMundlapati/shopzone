package com.shopzone.controller;

import com.shopzone.dto.response.*;
import com.shopzone.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Admin Reports API - Revenue, Sales, and User Growth reports.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Reports", description = "Revenue, sales, and user growth report APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

  private final ReportService reportService;


  @GetMapping("/revenue")
  @Operation(summary = "Get revenue report",
      description = "Get detailed revenue report with daily breakdown. Defaults to last 30 days.")
  public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueReport(
      @Parameter(description = "Start date (YYYY-MM-DD). Defaults to 30 days ago.")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @Parameter(description = "End date (YYYY-MM-DD). Defaults to today.")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    log.info("Admin requesting revenue report: {} to {}", startDate, endDate);
    RevenueReportResponse report = reportService.getRevenueReport(startDate, endDate);
    return ResponseEntity.ok(ApiResponse.success("Revenue report generated successfully", report));
  }


  @GetMapping("/sales")
  @Operation(summary = "Get sales report",
      description = "Get sales report with order breakdowns and top products. Defaults to last 30 days.")
  public ResponseEntity<ApiResponse<SalesReportResponse>> getSalesReport(
      @Parameter(description = "Start date (YYYY-MM-DD). Defaults to 30 days ago.")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @Parameter(description = "End date (YYYY-MM-DD). Defaults to today.")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    log.info("Admin requesting sales report: {} to {}", startDate, endDate);
    SalesReportResponse report = reportService.getSalesReport(startDate, endDate);
    return ResponseEntity.ok(ApiResponse.success("Sales report generated successfully", report));
  }


  @GetMapping("/user-growth")
  @Operation(summary = "Get user growth report",
      description = "Get user registration and growth report. Defaults to last 30 days.")
  public ResponseEntity<ApiResponse<UserGrowthResponse>> getUserGrowthReport(
      @Parameter(description = "Start date (YYYY-MM-DD). Defaults to 30 days ago.")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @Parameter(description = "End date (YYYY-MM-DD). Defaults to today.")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    log.info("Admin requesting user growth report: {} to {}", startDate, endDate);
    UserGrowthResponse report = reportService.getUserGrowthReport(startDate, endDate);
    return ResponseEntity.ok(ApiResponse.success("User growth report generated successfully", report));
  }
}