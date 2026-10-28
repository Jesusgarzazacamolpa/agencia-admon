package com.floworchestrator.vehicledomain.entity;

import com.floworchestrator.vehicledomain.domain.model.PurchaseOrderStatus;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "purchase_order",
        uniqueConstraints = @UniqueConstraint(name = "uk_purchase_order_purchase_order_id", columnNames = "purchase_order_id"),
        indexes = {
                @Index(name = "idx_purchase_order_customer", columnList = "customer_id"),
                @Index(name = "idx_purchase_order_vehicle", columnList = "vehicle_id")
        })
@Getter
@Setter
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "purchase_order_id", nullable = false, length = 100)
    private String purchaseOrderId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "vehicle_id", nullable = false, length = 100)
    private String vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PurchaseOrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void assignCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }
}
