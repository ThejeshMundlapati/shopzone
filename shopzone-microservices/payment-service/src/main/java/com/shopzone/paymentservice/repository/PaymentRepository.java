package com.shopzone.paymentservice.repository;

import com.shopzone.paymentservice.model.Payment;
import com.shopzone.paymentservice.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByStripePaymentIntentId(String intentId);
    Optional<Payment> findByOrderNumber(String orderNumber);
    Page<Payment> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);
    long countByStatus(PaymentStatus status);
    boolean existsByStripePaymentIntentId(String intentId);
}
