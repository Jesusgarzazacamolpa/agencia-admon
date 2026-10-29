package com.floworchestrator.vehicledomain.domain.model;

/**
 * Resultado del registro de un pago: orden afectada, estado alcanzado y tipo de pago.
 */
public record PaymentRegistered(String purchaseOrderId, PaymentStatus status, String paymentType) {
}
