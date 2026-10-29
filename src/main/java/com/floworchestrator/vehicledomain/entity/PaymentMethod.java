package com.floworchestrator.vehicledomain.entity;

import com.floworchestrator.vehicledomain.domain.model.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Entidad JPA de la tabla payment_method: registra el medio de pago configurado
 * para una orden de compra, con su tipo, institución financiera y estado.
 */
@Entity
@Table(name = "payment_method",
        indexes = @Index(name = "idx_payment_method_purchase_order", columnList = "purchase_order_id"))
@Getter
@Setter
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "purchase_order_id", nullable = false, length = 100)
    private String purchaseOrderId;

    @Column(name = "payment_type", nullable = false, length = 50)
    private String paymentType;

    @Column(name = "financial_institution", nullable = false, length = 150)
    private String financialInstitution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Asigna la fecha de creación en UTC si no viene informada.
     */
    @PrePersist
    void assignCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }
}
