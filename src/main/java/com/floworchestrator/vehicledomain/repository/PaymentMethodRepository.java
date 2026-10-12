package com.floworchestrator.vehicledomain.repository;

import com.floworchestrator.vehicledomain.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
}
