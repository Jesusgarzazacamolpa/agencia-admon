package com.floworchestrator.vehicledomain.domain.model;

public record CreatePurchaseOrderCommand(String vehicleId, String customerId, String purchaseOrderId) {
}
