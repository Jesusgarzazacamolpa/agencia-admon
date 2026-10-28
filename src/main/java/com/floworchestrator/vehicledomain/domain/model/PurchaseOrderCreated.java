package com.floworchestrator.vehicledomain.domain.model;

public record PurchaseOrderCreated(String purchaseOrderId, PurchaseOrderStatus status, String message) {
}
