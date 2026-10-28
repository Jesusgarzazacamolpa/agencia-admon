package com.floworchestrator.vehicledomain.domain.model;

public record RegisterPaymentCommand(String purchaseOrderId, String paymentType, String financialInstitution) {
}
