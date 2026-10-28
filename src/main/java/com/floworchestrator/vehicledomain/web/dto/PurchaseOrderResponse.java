package com.floworchestrator.vehicledomain.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PurchaseOrderResponse(
        @JsonProperty("purchase_order_id") String purchaseOrderId,
        String status,
        String message) {
}
