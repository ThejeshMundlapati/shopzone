package com.shopzone.repository;

import com.shopzone.model.Order;
import com.shopzone.model.enums.OrderStatus;
import com.shopzone.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

  // ========== Find by Order Number ==========

  Optional<Order> findByOrderNumber(String orderNumber);

  @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderNumber = :orderNumber")
  Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

  boolean existsByOrderNumber(String orderNumber);

  // ========== User Orders ==========

  Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status ORDER BY o.createdAt DESC")
  Page<Order> findByUserIdAndStatus(@Param("userId") Long userId,
                                    @Param("status") OrderStatus status,
                                    Pageable pageable);

  List<Order> findByUserIdAndStatusIn(Long userId, List<OrderStatus> statuses);

  long countByUserId(Long userId);

  // ========== Admin Queries ==========

  Page<Order> findByStatus(OrderStatus status, Pageable pageable);

  Page<Order> findByStatusIn(List<OrderStatus> statuses, Pageable pageable);

  @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt ASC")
  Page<Order> findByStatusOrderByCreatedAtAsc(@Param("status") OrderStatus status, Pageable pageable);

  @Query("SELECT o FROM Order o WHERE " +
      "(:status IS NULL OR o.status = :status) AND " +
      "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
      "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
      "(:endDate IS NULL OR o.createdAt <= :endDate) " +
      "ORDER BY o.createdAt DESC")
  Page<Order> findWithFilters(@Param("status") OrderStatus status,
                              @Param("paymentStatus") PaymentStatus paymentStatus,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              Pageable pageable);

  // ========== Statistics Queries ==========

  long countByStatus(OrderStatus status);

  long countByStatusIn(List<OrderStatus> statuses);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :since")
  long countOrdersSince(@Param("since") LocalDateTime since);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :start AND o.createdAt < :end")
  long countOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  // ========== Revenue Queries ==========

  @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAID'")
  BigDecimal getTotalRevenue();

  @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAID' AND o.paidAt >= :since")
  BigDecimal getRevenueSince(@Param("since") LocalDateTime since);

  @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAID' AND o.paidAt >= :start AND o.paidAt < :end")
  BigDecimal getRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAID'")
  BigDecimal getAverageOrderValue();

  // ========== Status Counts for Dashboard ==========

  @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
  List<Object[]> getOrderCountsByStatus();

  // ========== Search ==========

  @Query("SELECT o FROM Order o WHERE " +
      "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(o.userEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(o.userFullName) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<Order> searchOrders(@Param("search") String search, Pageable pageable);
}