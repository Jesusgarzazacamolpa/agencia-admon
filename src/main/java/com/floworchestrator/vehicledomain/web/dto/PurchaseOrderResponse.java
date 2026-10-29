package com.floworchestrator.vehicledomain.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta del alta de una orden de compra: identificador, estado y mensaje descriptivo.
 */
public record PurchaseOrderResponse(
        @JsonProperty("purchase_order_id") String purchaseOrderId,
        String status,
        String message) {
}
