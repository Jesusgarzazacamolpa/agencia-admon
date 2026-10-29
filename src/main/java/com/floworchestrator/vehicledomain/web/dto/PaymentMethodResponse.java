package com.floworchestrator.vehicledomain.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta del registro de un pago: orden afectada, estado alcanzado y tipo de pago.
 */
public record PaymentMethodResponse(
        @JsonProperty("purchase_order_id") String purchaseOrderId,
        String status,
        @JsonProperty("payment_type") String paymentType) {
}
