package com.shopzone.orderservice.saga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SagaStateRepository extends JpaRepository<SagaState, String> {

    Optional<SagaState> findByOrderNumber(String orderNumber);

    List<SagaState> findByStatus(String status);

    boolean existsByOrderNumber(String orderNumber);
}
