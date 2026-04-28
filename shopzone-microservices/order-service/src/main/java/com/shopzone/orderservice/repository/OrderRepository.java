package com.shopzone.orderservice.repository;

import com.shopzone.orderservice.model.Order;
import com.shopzone.orderservice.model.enums.OrderStatus;
import com.shopzone.orderservice.model.enums.PaymentStatus;
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
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderNumber = :num")
    Optional<Order> findByOrderNumberWithItems(@Param("num") String orderNumber);

    Page<Order> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Page<Order> findByUserIdAndStatus(String userId, OrderStatus status, Pageable pageable);
    long countByUserId(String userId);
    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);
    boolean existsByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE " +
        "(:status IS NULL OR o.status = :status) AND " +
        "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
        "(CAST(:startDate AS timestamp) IS NULL OR o.createdAt >= :startDate) AND " +
        "(CAST(:endDate AS timestamp) IS NULL OR o.createdAt <= :endDate)")
    Page<Order> findWithFilters(@Param("status") OrderStatus status, @Param("paymentStatus") PaymentStatus paymentStatus,
        @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE LOWER(o.orderNumber) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
        "LOWER(o.userEmail) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(o.userFullName) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Order> searchOrders(@Param("q") String query, Pageable pageable);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderCountsByStatus();

    @Query(value = "SELECT SUM(total_amount) FROM orders WHERE status NOT IN ('CANCELLED','REFUNDED')", nativeQuery = true)
    BigDecimal getTotalRevenue();

    long countByCreatedAtAfter(LocalDateTime dt);
    long countByStatus(OrderStatus status);
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.totalAmount),0) FROM Order o WHERE o.paymentStatus = 'PAID' AND o.createdAt >= :since")
    BigDecimal calculateRevenueSince(@Param("since") LocalDateTime since);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(Pageable pageable);

    List<Order> findByStatusIn(List<OrderStatus> statuses);
    List<Order> findByPaymentStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount),0) FROM Order o WHERE o.userId = :uid AND o.paymentStatus = 'PAID'")
    BigDecimal getTotalSpentByUser(@Param("uid") String userId);

    @Query("SELECT MAX(o.createdAt) FROM Order o WHERE o.userId = :uid")
    LocalDateTime findLastOrderDateByUserId(@Param("uid") String userId);
}
