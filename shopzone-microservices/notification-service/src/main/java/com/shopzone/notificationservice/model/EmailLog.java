package com.shopzone.notificationservice.model;

import com.shopzone.notificationservice.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "email_logs")
public class EmailLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    private String recipientEmail, subject, referenceId, userId;
    @Enumerated(EnumType.STRING) private EmailType emailType;
    @Enumerated(EnumType.STRING) @Builder.Default private EmailStatus status = EmailStatus.PENDING;
    @Column(length = 1000) private String errorMessage;
    @Builder.Default private int retryCount = 0;
    @CreationTimestamp @Column(updatable = false) private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public void markAsSent() { this.status = EmailStatus.SENT; this.sentAt = LocalDateTime.now(); }
    public void markAsFailed(String error) { this.status = EmailStatus.FAILED; this.errorMessage = error; this.retryCount++; }
}
