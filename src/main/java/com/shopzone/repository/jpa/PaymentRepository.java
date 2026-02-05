package com.shopzone.repository.jpa;

import com.shopzone.model.Payment;
import com.shopzone.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {


  /**
   * Find payment by Stripe Payment Intent ID.
   */
  Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);

  /**
   * Find payment by Stripe Charge ID.
   */
  Optional<Payment> findByStripeChargeId(String chargeId);


  /**
   * Find all payments for an order.
   */
  List<Payment> findByOrderIdOrderByCreatedAtDesc(String orderId);

  /**
   * Find payment by order number.
   */
  Optional<Payment> findByOrderNumber(String orderNumber);

  /**
   * Find the most recent payment for an order.
   */
  Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc(String orderId);


  /**
   * Find all payments for a user.
   */
  Page<Payment> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

  /**
   * Find payments by user and status.
   */
  Page<Payment> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, PaymentStatus status, Pageable pageable);


  /**
   * Find payments by status.
   */
  Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

  /**
   * Find pending payments older than specified time (for cleanup).
   */
  @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :cutoff")
  List<Payment> findStalePendingPayments(@Param("cutoff") LocalDateTime cutoff);


  /**
   * Count payments by status.
   */
  long countByStatus(PaymentStatus status);

  /**
   * Sum of successful payments in a date range.
   */
  @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
      "WHERE p.status = 'PAID' AND p.paidAt BETWEEN :start AND :end")
  java.math.BigDecimal sumPaidAmountBetween(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end
  );

  /**
   * Sum of refunds in a date range.
   */
  @Query("SELECT COALESCE(SUM(p.amountRefunded), 0) FROM Payment p " +
      "WHERE p.amountRefunded > 0 AND p.refundedAt BETWEEN :start AND :end")
  java.math.BigDecimal sumRefundedAmountBetween(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end
  );

  /**
   * Count successful payments in a date range.
   */
  @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'PAID' AND p.paidAt BETWEEN :start AND :end")
  long countPaidBetween(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end
  );


  /**
   * Search payments (admin).
   */
  @Query("SELECT p FROM Payment p WHERE " +
      "(:status IS NULL OR p.status = :status) AND " +
      "(:userId IS NULL OR p.userId = :userId) AND " +
      "(:startDate IS NULL OR p.createdAt >= :startDate) AND " +
      "(:endDate IS NULL OR p.createdAt <= :endDate) " +
      "ORDER BY p.createdAt DESC")
  Page<Payment> searchPayments(
      @Param("status") PaymentStatus status,
      @Param("userId") String userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable
  );

  /**
   * Check if payment intent already exists (idempotency).
   */
  boolean existsByStripePaymentIntentId(String paymentIntentId);
}