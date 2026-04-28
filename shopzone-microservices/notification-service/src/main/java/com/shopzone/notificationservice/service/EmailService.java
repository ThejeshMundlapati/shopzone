package com.shopzone.notificationservice.service;

import com.shopzone.notificationservice.model.EmailLog;
import com.shopzone.notificationservice.model.enums.*;
import com.shopzone.notificationservice.repository.EmailLogRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service @RequiredArgsConstructor @Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailLogRepository emailLogRepository;

    @Value("${shopzone.email.from:noreply@shopzone.com}") private String fromEmail;
    @Value("${shopzone.app.name:ShopZone}") private String appName;
    @Value("${shopzone.app.url:http://localhost:3000}") private String appUrl;

    @Async
    public void sendWelcome(String email, String firstName) {
        Context ctx = new Context();
        ctx.setVariable("customerName", firstName);
        ctx.setVariable("appName", appName);
        ctx.setVariable("appUrl", appUrl);
        sendHtml(email, "Welcome to " + appName + "!", "welcome", ctx, EmailType.WELCOME, null);
    }

    @Async
    public void sendPasswordReset(String email, String firstName, String token) {
        Context ctx = new Context();
        ctx.setVariable("customerName", firstName);
        ctx.setVariable("resetUrl", appUrl + "/reset-password?token=" + token);
        ctx.setVariable("appName", appName);
        sendHtml(email, appName + " - Password Reset", "password-reset", ctx, EmailType.PASSWORD_RESET, null);
    }

    @Async
    public void sendOrderConfirmation(String email, String customerName, String orderNumber) {
        Context ctx = new Context();
        ctx.setVariable("customerName", customerName);
        ctx.setVariable("orderNumber", orderNumber);
        ctx.setVariable("appName", appName);
        ctx.setVariable("appUrl", appUrl);
        sendHtml(email, appName + " - Order Confirmation #" + orderNumber, "order-confirmation", ctx, EmailType.ORDER_CONFIRMATION, orderNumber);
    }

    @Async
    public void sendOrderShipped(String email, String customerName, String orderNumber, String tracking, String carrier) {
        Context ctx = new Context();
        ctx.setVariable("customerName", customerName);
        ctx.setVariable("orderNumber", orderNumber);
        ctx.setVariable("trackingNumber", tracking);
        ctx.setVariable("shippingCarrier", carrier);
        ctx.setVariable("appName", appName);
        sendHtml(email, appName + " - Order Shipped #" + orderNumber, "order-shipped", ctx, EmailType.ORDER_SHIPPED, orderNumber);
    }

    @Async
    public void sendOrderDelivered(String email, String customerName, String orderNumber) {
        Context ctx = new Context();
        ctx.setVariable("customerName", customerName);
        ctx.setVariable("orderNumber", orderNumber);
        ctx.setVariable("appName", appName);
        sendHtml(email, appName + " - Order Delivered #" + orderNumber, "order-delivered", ctx, EmailType.ORDER_DELIVERED, orderNumber);
    }

    @Async
    public void sendOrderCancelled(String email, String customerName, String orderNumber, String reason) {
        Context ctx = new Context();
        ctx.setVariable("customerName", customerName);
        ctx.setVariable("orderNumber", orderNumber);
        ctx.setVariable("cancellationReason", reason);
        ctx.setVariable("appName", appName);
        sendHtml(email, appName + " - Order Cancelled #" + orderNumber, "order-cancellation", ctx, EmailType.ORDER_CANCELLED, orderNumber);
    }

    private void sendHtml(String to, String subject, String template, Context ctx, EmailType type, String refId) {
        EmailLog logEntry = emailLogRepository.save(EmailLog.builder()
            .recipientEmail(to).subject(subject).emailType(type).referenceId(refId).build());
        try {
            String html = templateEngine.process(template, ctx);
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail); helper.setTo(to); helper.setSubject(subject); helper.setText(html, true);
            mailSender.send(msg);
            logEntry.markAsSent(); emailLogRepository.save(logEntry);
            log.info("Email sent: {} to {}", type, to);
        } catch (Exception e) {
            logEntry.markAsFailed(e.getMessage()); emailLogRepository.save(logEntry);
            log.error("Email failed: {} to {} - {}", type, to, e.getMessage());
        }
    }
}
