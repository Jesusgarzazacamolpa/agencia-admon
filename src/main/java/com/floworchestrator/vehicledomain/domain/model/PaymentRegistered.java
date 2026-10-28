package com.floworchestrator.vehicledomain.domain.model;

public record PaymentRegistered(String purchaseOrderId, PaymentStatus status, String paymentType) {
}
