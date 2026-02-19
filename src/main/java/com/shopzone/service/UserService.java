package com.shopzone.service;

import com.shopzone.dto.response.UserResponse;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.User;
import com.shopzone.repository.jpa.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final EmailService emailService;

  public UserService(UserRepository userRepository, @Lazy EmailService emailService) {
    this.userRepository = userRepository;
    this.emailService = emailService;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository.findByEmail(email.toLowerCase())
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
  }

  public UserResponse getUserById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    return UserResponse.from(user);
  }

  public UserResponse getUserByEmail(String email) {
    User user = userRepository.findByEmail(email.toLowerCase())
        .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    return UserResponse.from(user);
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email.toLowerCase())
        .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
  }

  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email.toLowerCase());
  }


  /**
   * Send welcome email to newly registered user.
   * Called by AuthService after successful registration.
   */
  public void sendWelcomeEmail(User user) {
    try {
      emailService.sendWelcomeEmail(user);
      log.info("Welcome email queued for user: {}", user.getEmail());
    } catch (Exception e) {
      log.error("Failed to send welcome email to: {}", user.getEmail(), e);
    }
  }

  /**
   * Send password reset email.
   * Called by AuthService when user requests password reset.
   */
  public void sendPasswordResetEmail(User user, String resetToken) {
    try {
      emailService.sendPasswordResetEmail(user, resetToken);
      log.info("Password reset email queued for user: {}", user.getEmail());
    } catch (Exception e) {
      log.error("Failed to send password reset email to: {}", user.getEmail(), e);
    }
  }

}