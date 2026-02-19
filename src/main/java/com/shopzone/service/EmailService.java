package com.shopzone.service;

import com.shopzone.model.AddressSnapshot;
import com.shopzone.model.EmailLog;
import com.shopzone.model.Order;
import com.shopzone.model.OrderItem;
import com.shopzone.model.User;
import com.shopzone.model.enums.EmailStatus;
import com.shopzone.model.enums.EmailType;
import com.shopzone.repository.jpa.EmailLogRepository;
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

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending transactional emails.
 * Uses Thymeleaf templates and Mailtrap for testing.
 * All email sending is async to not block the main thread.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private final EmailLogRepository emailLogRepository;

  @Value("${spring.mail.properties.mail.smtp.from:noreply@shopzone.com}")
  private String fromEmail;

  @Value("${shopzone.app.name:ShopZone}")
  private String appName;

  @Value("${shopzone.app.url:http://localhost:8080}")
  private String appUrl;

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
  private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a");


  @Async
  public void sendOrderConfirmationEmail(User user, Order order) {
    try {
      log.info("Sending order confirmation email for order: {}", order.getOrderNumber());

      Context context = new Context();
      context.setVariable("customerName", user.getFirstName() + " " + user.getLastName());
      context.setVariable("orderNumber", order.getOrderNumber());
      context.setVariable("orderDate", order.getCreatedAt().format(DATETIME_FORMAT));
      context.setVariable("items", order.getItems());
      context.setVariable("subtotal", order.getSubtotal());
      context.setVariable("taxAmount", order.getTaxAmount());
      context.setVariable("shippingCost", order.getShippingCost());
      context.setVariable("discountAmount", order.getDiscountAmount());
      context.setVariable("totalAmount", order.getTotalAmount());
      context.setVariable("appName", appName);
      context.setVariable("appUrl", appUrl);

      AddressSnapshot addr = order.getShippingAddress();
      if (addr != null) {
        context.setVariable("shippingName", addr.getFullName());
        context.setVariable("shippingAddress", addr.getFormattedAddress());
        context.setVariable("shippingPhone", addr.getPhoneNumber());
      }

      context.setVariable("itemDetails", buildItemDetails(order));

      String htmlContent = templateEngine.process("order-confirmation", context);
      sendHtmlEmail(
          user.getEmail(),
          appName + " - Order Confirmation #" + order.getOrderNumber(),
          htmlContent,
          EmailType.ORDER_CONFIRMATION,
          order.getOrderNumber(),
          user.getId().toString()
      );

      log.info("Order confirmation email sent successfully for order: {}", order.getOrderNumber());
    } catch (Exception e) {
      log.error("Failed to send order confirmation email for order: {}", order.getOrderNumber(), e);
    }
  }


  @Async
  public void sendShippingUpdateEmail(User user, Order order) {
    try {
      log.info("Sending shipping update email for order: {}", order.getOrderNumber());

      Context context = new Context();
      context.setVariable("customerName", user.getFirstName());
      context.setVariable("orderNumber", order.getOrderNumber());
      context.setVariable("trackingNumber", order.getTrackingNumber());
      context.setVariable("shippingCarrier", order.getShippingCarrier());
      context.setVariable("shippedDate", order.getShippedAt() != null
          ? order.getShippedAt().format(DATETIME_FORMAT) : "N/A");
      context.setVariable("appName", appName);
      context.setVariable("appUrl", appUrl);

      AddressSnapshot addr = order.getShippingAddress();
      if (addr != null) {
        context.setVariable("shippingName", addr.getFullName());
        context.setVariable("shippingAddress", addr.getFormattedAddress());
      }

      String htmlContent = templateEngine.process("order-shipped", context);
      sendHtmlEmail(
          user.getEmail(),
          appName + " - Your Order Has Shipped! #" + order.getOrderNumber(),
          htmlContent,
          EmailType.ORDER_SHIPPED,
          order.getOrderNumber(),
          user.getId().toString()
      );

      log.info("Shipping update email sent successfully for order: {}", order.getOrderNumber());
    } catch (Exception e) {
      log.error("Failed to send shipping update email for order: {}", order.getOrderNumber(), e);
    }
  }


  @Async
  public void sendDeliveryConfirmationEmail(User user, Order order) {
    try {
      log.info("Sending delivery confirmation email for order: {}", order.getOrderNumber());

      Context context = new Context();
      context.setVariable("customerName", user.getFirstName());
      context.setVariable("orderNumber", order.getOrderNumber());
      context.setVariable("deliveredDate", order.getDeliveredAt() != null
          ? order.getDeliveredAt().format(DATETIME_FORMAT) : "N/A");
      context.setVariable("appName", appName);
      context.setVariable("appUrl", appUrl);

      String htmlContent = templateEngine.process("order-delivered", context);
      sendHtmlEmail(
          user.getEmail(),
          appName + " - Order Delivered! #" + order.getOrderNumber(),
          htmlContent,
          EmailType.ORDER_DELIVERED,
          order.getOrderNumber(),
          user.getId().toString()
      );

      log.info("Delivery confirmation email sent successfully for order: {}", order.getOrderNumber());
    } catch (Exception e) {
      log.error("Failed to send delivery confirmation email for order: {}", order.getOrderNumber(), e);
    }
  }


  @Async
  public void sendOrderCancellationEmail(User user, Order order) {
    try {
      log.info("Sending cancellation email for order: {}", order.getOrderNumber());

      Context context = new Context();
      context.setVariable("customerName", user.getFirstName());
      context.setVariable("orderNumber", order.getOrderNumber());
      context.setVariable("cancellationReason", order.getCancellationReason());
      context.setVariable("cancelledBy", order.getCancelledBy());
      context.setVariable("totalAmount", order.getTotalAmount());
      context.setVariable("appName", appName);
      context.setVariable("appUrl", appUrl);

      String htmlContent = templateEngine.process("order-cancellation", context);
      sendHtmlEmail(
          user.getEmail(),
          appName + " - Order Cancelled #" + order.getOrderNumber(),
          htmlContent,
          EmailType.ORDER_CANCELLED,
          order.getOrderNumber(),
          user.getId().toString()
      );

      log.info("Cancellation email sent successfully for order: {}", order.getOrderNumber());
    } catch (Exception e) {
      log.error("Failed to send cancellation email for order: {}", order.getOrderNumber(), e);
    }
  }


  @Async
  public void sendWelcomeEmail(User user) {
    try {
      log.info("Sending welcome email to: {}", user.getEmail());

      Context context = new Context();
      context.setVariable("customerName", user.getFirstName());
      context.setVariable("email", user.getEmail());
      context.setVariable("appName", appName);
      context.setVariable("appUrl", appUrl);

      String htmlContent = templateEngine.process("welcome", context);
      sendHtmlEmail(
          user.getEmail(),
          "Welcome to " + appName + "!",
          htmlContent,
          EmailType.WELCOME,
          user.getId().toString(),
          user.getId().toString()
      );

      log.info("Welcome email sent successfully to: {}", user.getEmail());
    } catch (Exception e) {
      log.error("Failed to send welcome email to: {}", user.getEmail(), e);
    }
  }


  @Async
  public void sendPasswordResetEmail(User user, String resetToken) {
    try {
      log.info("Sending password reset email to: {}", user.getEmail());

      String resetUrl = appUrl + "/reset-password?token=" + resetToken;

      Context context = new Context();
      context.setVariable("customerName", user.getFirstName());
      context.setVariable("resetUrl", resetUrl);
      context.setVariable("appName", appName);
      context.setVariable("appUrl", appUrl);

      String htmlContent = templateEngine.process("password-reset", context);
      sendHtmlEmail(
          user.getEmail(),
          appName + " - Password Reset Request",
          htmlContent,
          EmailType.PASSWORD_RESET,
          user.getId().toString(),
          user.getId().toString()
      );

      log.info("Password reset email sent successfully to: {}", user.getEmail());
    } catch (Exception e) {
      log.error("Failed to send password reset email to: {}", user.getEmail(), e);
    }
  }


  @Async
  public void sendEmailVerificationEmail(User user, String verificationToken) {
    try {
      log.info("Sending email verification to: {}", user.getEmail());

      String verifyUrl = appUrl + "/api/auth/verify/" + verificationToken;

      Context context = new Context();
      context.setVariable("customerName", user.getFirstName());
      context.setVariable("verifyUrl", verifyUrl);
      context.setVariable("appName", appName);
      context.setVariable("appUrl", appUrl);

      String htmlContent = templateEngine.process("email-verification", context);
      sendHtmlEmail(
          user.getEmail(),
          appName + " - Verify Your Email",
          htmlContent,
          EmailType.EMAIL_VERIFICATION,
          user.getId().toString(),
          user.getId().toString()
      );

      log.info("Email verification sent successfully to: {}", user.getEmail());
    } catch (Exception e) {
      log.error("Failed to send email verification to: {}", user.getEmail(), e);
    }
  }


  @Async
  public void sendOrderConfirmation(Order order, User user) {
    sendOrderConfirmationEmail(user, order);
  }

  @Async
  public void sendOrderShipped(Order order, User user, String trackingNumber, String carrier) {
    sendShippingUpdateEmail(user, order);
  }

  @Async
  public void sendOrderDelivered(Order order, User user) {
    sendDeliveryConfirmationEmail(user, order);
  }

  @Async
  public void sendOrderCancelled(Order order, User user, String reason, BigDecimal refundAmount) {
    sendOrderCancellationEmail(user, order);
  }


  private void sendHtmlEmail(String to, String subject, String htmlContent, EmailType emailType, String referenceId, String userId) {
    EmailLog emailLog = EmailLog.builder()
        .recipientEmail(to)
        .subject(subject)
        .emailType(emailType)
        .referenceId(referenceId)
        .userId(userId)
        .status(EmailStatus.PENDING)
        .build();

    emailLog = emailLogRepository.save(emailLog);

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);

      mailSender.send(message);

      emailLog.markAsSent();
      emailLogRepository.save(emailLog);

    } catch (Exception e) {
      log.error("Failed to send email to {}", to, e);

      emailLog.markAsFailed(e.getMessage());
      emailLogRepository.save(emailLog);


    }
  }

  /**
   * Build item details list for email template.
   */
  private java.util.List<Map<String, Object>> buildItemDetails(Order order) {
    java.util.List<Map<String, Object>> items = new java.util.ArrayList<>();

    for (OrderItem item : order.getItems()) {
      Map<String, Object> itemMap = new HashMap<>();
      itemMap.put("name", item.getProductName());
      itemMap.put("quantity", item.getQuantity());
      itemMap.put("unitPrice", item.getEffectivePrice());
      itemMap.put("totalPrice", item.getTotalPrice());
      itemMap.put("image", item.getProductImage());
      itemMap.put("sku", item.getProductSku());
      items.add(itemMap);
    }

    return items;
  }
}