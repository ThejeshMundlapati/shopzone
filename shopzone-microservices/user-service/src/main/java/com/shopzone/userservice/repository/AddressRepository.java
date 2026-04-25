package com.shopzone.userservice.repository;

import com.shopzone.userservice.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    List<Address> findByUserIdAndActiveTrueOrderByIsDefaultDescCreatedAtDesc(String userId);
    Optional<Address> findByUserIdAndIsDefaultTrueAndActiveTrue(String userId);
    Optional<Address> findByIdAndUserIdAndActiveTrue(String id, String userId);
    long countByUserIdAndActiveTrue(String userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userId = :userId")
    void clearDefaultForUser(@Param("userId") String userId);
}
