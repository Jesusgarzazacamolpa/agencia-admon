package com.floworchestrator.vehicledomain.domain.model;

/**
 * Resultado del registro de una orden de compra, con el estado y un mensaje descriptivo.
 */
public record PurchaseOrderCreated(String purchaseOrderId, PurchaseOrderStatus status, String message) {
}
