package com.floworchestrator.vehicledomain.domain.model;

/**
 * Comando de dominio para registrar un pago asociado a una orden de compra existente.
 */
public record RegisterPaymentCommand(String purchaseOrderId, String paymentType, String financialInstitution) {
}
