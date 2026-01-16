package com.shopzone.service;

import com.shopzone.dto.response.UserResponse;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.User;
import com.shopzone.repository.jpa.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
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
}