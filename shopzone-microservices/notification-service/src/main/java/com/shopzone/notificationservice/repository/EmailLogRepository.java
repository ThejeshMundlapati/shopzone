package com.shopzone.notificationservice.repository;
import com.shopzone.notificationservice.model.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {}
