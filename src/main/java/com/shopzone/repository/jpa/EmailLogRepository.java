package com.shopzone.repository.jpa;

import com.shopzone.model.EmailLog;
import com.shopzone.model.enums.EmailStatus;
import com.shopzone.model.enums.EmailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {

  Page<EmailLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

  Page<EmailLog> findByStatusOrderByCreatedAtDesc(EmailStatus status, Pageable pageable);

  Page<EmailLog> findByEmailTypeOrderByCreatedAtDesc(EmailType emailType, Pageable pageable);

  List<EmailLog> findByStatusAndRetryCountLessThan(EmailStatus status, int maxRetries);

  long countByStatus(EmailStatus status);

  @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.status = :status " +
      "AND e.createdAt >= :startDate AND e.createdAt < :endDate")
  long countByStatusAndDateRange(
      @Param("status") EmailStatus status,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.status = 'SENT' " +
      "AND e.createdAt >= :startOfDay")
  long countSentToday(@Param("startOfDay") LocalDateTime startOfDay);

  @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.status = 'FAILED' " +
      "AND e.createdAt >= :startOfDay")
  long countFailedToday(@Param("startOfDay") LocalDateTime startOfDay);

  List<EmailLog> findByReferenceIdOrderByCreatedAtDesc(String referenceId);

  @Query("SELECT e.emailType, COUNT(e) FROM EmailLog e " +
      "WHERE e.createdAt >= :startDate " +
      "GROUP BY e.emailType")
  List<Object[]> countByEmailTypeSince(@Param("startDate") LocalDateTime startDate);

  @Query("SELECT e FROM EmailLog e WHERE " +
      "(:email IS NULL OR LOWER(e.recipientEmail) LIKE LOWER(CONCAT('%', :email, '%'))) " +
      "AND (:status IS NULL OR e.status = :status) " +
      "AND (:type IS NULL OR e.emailType = :type) " +
      "ORDER BY e.createdAt DESC")
  Page<EmailLog> searchEmails(
      @Param("email") String email,
      @Param("status") EmailStatus status,
      @Param("type") EmailType type,
      Pageable pageable);
}