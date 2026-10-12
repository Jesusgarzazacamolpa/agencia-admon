package com.floworchestrator.vehicledomain.repository;

import com.floworchestrator.vehicledomain.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    Optional<PurchaseOrder> findByPurchaseOrderId(String purchaseOrderId);
}
