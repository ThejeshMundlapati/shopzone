package com.shopzone.repository.jpa;

import com.shopzone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}