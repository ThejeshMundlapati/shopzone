package com.shopzone.model;

import com.shopzone.model.enums.EmailStatus;
import com.shopzone.model.enums.EmailType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_logs", indexes = {
    @Index(name = "idx_email_log_user_id", columnList = "user_id"),
    @Index(name = "idx_email_log_type", columnList = "email_type"),
    @Index(name = "idx_email_log_status", columnList = "status"),
    @Index(name = "idx_email_log_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "recipient_email", nullable = false)
  private String recipientEmail;

  @Column(name = "recipient_name")
  private String recipientName;

  @Enumerated(EnumType.STRING)
  @Column(name = "email_type", nullable = false)
  private EmailType emailType;

  @Column(nullable = false)
  private String subject;

  @Column(name = "template_name")
  private String templateName;

  @Column(name = "reference_id")
  private String referenceId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private EmailStatus status = EmailStatus.PENDING;

  @Column(name = "error_message", length = 1000)
  private String errorMessage;

  @Column(name = "retry_count")
  @Builder.Default
  private Integer retryCount = 0;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  /**
   * Mark email as sent successfully.
   */
  public void markAsSent() {
    this.status = EmailStatus.SENT;
    this.sentAt = LocalDateTime.now();
  }

  /**
   * Mark email as failed.
   */
  public void markAsFailed(String errorMessage) {
    this.status = EmailStatus.FAILED;
    this.errorMessage = errorMessage;
    this.retryCount++;
  }

  /**
   * Check if email can be retried.
   */
  public boolean canRetry(int maxRetries) {
    return this.retryCount < maxRetries && this.status == EmailStatus.FAILED;
  }
}