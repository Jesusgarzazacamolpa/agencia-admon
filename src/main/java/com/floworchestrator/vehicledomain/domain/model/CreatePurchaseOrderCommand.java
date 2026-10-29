package com.floworchestrator.vehicledomain.domain.model;

/**
 * Comando de dominio para registrar una orden de compra de un vehículo.
 * El identificador de orden es opcional; si no se informa, el servicio genera uno.
 */
public record CreatePurchaseOrderCommand(String vehicleId, String customerId, String purchaseOrderId) {
}
