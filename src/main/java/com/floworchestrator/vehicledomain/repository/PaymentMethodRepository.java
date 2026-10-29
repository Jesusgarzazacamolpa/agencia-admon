package com.floworchestrator.vehicledomain.repository;

import com.floworchestrator.vehicledomain.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio JPA de los métodos de pago registrados.
 * Solo usa las operaciones CRUD heredadas; no define consultas derivadas.
 */
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
}
