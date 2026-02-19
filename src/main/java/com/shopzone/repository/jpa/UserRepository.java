package com.shopzone.repository.jpa;

import com.shopzone.model.Role;
import com.shopzone.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

  @Modifying
  @Query("UPDATE User u SET u.refreshToken = :refreshToken WHERE u.id = :userId")
  void updateRefreshToken(@Param("userId") UUID userId, @Param("refreshToken") String refreshToken);

  @Modifying
  @Query("UPDATE User u SET u.emailVerified = true, u.verificationToken = null, u.verificationTokenExpiry = null WHERE u.id = :userId")
  void verifyEmail(@Param("userId") UUID userId);


  long countByCreatedAtAfter(LocalDateTime date);

  long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

  List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

  long countByRole(Role role);

  long countByEmailVerifiedTrue();


  /**
   * Find users by role with pagination.
   */
  Page<User> findByRole(Role role, Pageable pageable);

  /**
   * Search users by email, first name, or last name.
   */
  @Query("SELECT u FROM User u WHERE " +
      "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<User> searchUsers(@Param("search") String search, Pageable pageable);

  /**
   * Find users by role AND search term.
   */
  @Query("SELECT u FROM User u WHERE u.role = :role AND (" +
      "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
  Page<User> findByRoleAndSearch(@Param("role") Role role,
                                 @Param("search") String search,
                                 Pageable pageable);

  /**
   * Count enabled users.
   */
  long countByEnabledTrue();

  /**
   * Count locked users.
   */
  long countByLockedTrue();
}