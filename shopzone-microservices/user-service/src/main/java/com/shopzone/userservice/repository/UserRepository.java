package com.shopzone.userservice.repository;

import com.shopzone.userservice.model.Role;
import com.shopzone.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
    Optional<User> findByRefreshToken(String refreshToken);

    long countByCreatedAtAfter(LocalDateTime date);
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByRole(Role role);
    long countByEmailVerifiedTrue();
    long countByEnabledTrue();
    long countByLockedTrue();

    Page<User> findByRole(Role role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
        "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND (" +
        "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findByRoleAndSearch(@Param("role") Role role, @Param("search") String search, Pageable pageable);
}
