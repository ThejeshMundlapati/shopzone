package com.shopzone.userservice.service;

import com.shopzone.common.dto.response.UserResponse;
import com.shopzone.common.exception.ResourceNotFoundException;
import com.shopzone.userservice.model.User;
import com.shopzone.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    /**
     * Get user by ID — called internally by other microservices via /api/internal/users/{id}
     */
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return toUserResponse(user);
    }

    /**
     * Get user by email — called internally by other microservices
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return toUserResponse(user);
    }

    /**
     * Get the raw User entity — used within this service only
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private UserResponse toUserResponse(User u) {
        return UserResponse.builder()
                .id(u.getId().toString()).firstName(u.getFirstName()).lastName(u.getLastName())
                .email(u.getEmail()).phone(u.getPhone()).role(u.getRole().name())
                .emailVerified(u.getEmailVerified()).enabled(u.getEnabled()).createdAt(u.getCreatedAt())
                .build();
    }
}
