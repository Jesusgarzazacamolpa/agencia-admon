package com.floworchestrator.vehicledomain.repository;

import com.floworchestrator.vehicledomain.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA de las órdenes de compra, indexadas por el identificador
 * de negocio purchase_order_id además de la clave técnica.
 */
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    /**
     * Busca una orden por su identificador de negocio purchase_order_id.
     */
    Optional<PurchaseOrder> findByPurchaseOrderId(String purchaseOrderId);

    /**
     * Indica si ya existe una orden con el identificador de negocio indicado.
     */
    boolean existsByPurchaseOrderId(String purchaseOrderId);
}
