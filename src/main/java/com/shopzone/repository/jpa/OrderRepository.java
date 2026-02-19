package com.shopzone.repository.jpa;

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


  @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderNumber = :orderNumber")
  Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

  Page<Order> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

  Page<Order> findByUserIdAndStatus(String userId, OrderStatus status, Pageable pageable);

  long countByUserId(String userId);

  @Query("SELECT o FROM Order o WHERE " +
      "(:status IS NULL OR o.status = :status) AND " +
      "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
      "(CAST(:startDate AS timestamp) IS NULL OR o.createdAt >= :startDate) AND " +
      "(CAST(:endDate AS timestamp) IS NULL OR o.createdAt <= :endDate)")
  Page<Order> findWithFilters(@Param("status") OrderStatus status,
                              @Param("paymentStatus") PaymentStatus paymentStatus,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              Pageable pageable);

  @Query("SELECT o FROM Order o WHERE " +
      "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
      "LOWER(o.userEmail) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
      "LOWER(o.userFullName) LIKE LOWER(CONCAT('%', :query, '%'))")
  Page<Order> searchOrders(@Param("query") String query, Pageable pageable);

  @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
  List<Object[]> getOrderCountsByStatus();

  @Query(value = "SELECT SUM(total_amount) FROM orders WHERE status NOT IN ('CANCELLED', 'REFUNDED')", nativeQuery = true)
  BigDecimal getTotalRevenue();

  @Query(value = "SELECT AVG(total_amount) FROM orders WHERE status NOT IN ('CANCELLED', 'REFUNDED')", nativeQuery = true)
  BigDecimal getAverageOrderValue();

  long countByCreatedAtAfter(LocalDateTime dateTime);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startOfDay")
  long countOrdersToday(@Param("startOfDay") LocalDateTime startOfDay);

  boolean existsByOrderNumber(String orderNumber);


  List<Order> findByUserIdAndStatus(String userId, OrderStatus status);


  long countByStatus(OrderStatus status);

  long countByPaymentStatus(PaymentStatus status);

  @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
      "WHERE o.paymentStatus = 'PAID' AND o.createdAt >= :since")
  BigDecimal calculateRevenueSince(@Param("since") LocalDateTime since);

  List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

  @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items ORDER BY o.createdAt DESC")
  List<Order> findRecentOrders(Pageable pageable);

  List<Order> findByStatusIn(List<OrderStatus> statuses);

  List<Order> findByPaymentStatus(PaymentStatus paymentStatus);

  List<Order> findByPaymentStatusAndCreatedAtBetween(
      PaymentStatus status, LocalDateTime start, LocalDateTime end);


  /**
   * Get total amount spent by a user (from paid orders).
   */
  @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
      "WHERE o.userId = :userId AND o.paymentStatus = 'PAID'")
  BigDecimal getTotalSpentByUser(@Param("userId") String userId);

  /**
   * Get the date of the user's most recent order.
   */
  @Query("SELECT MAX(o.createdAt) FROM Order o WHERE o.userId = :userId")
  LocalDateTime findLastOrderDateByUserId(@Param("userId") String userId);
}