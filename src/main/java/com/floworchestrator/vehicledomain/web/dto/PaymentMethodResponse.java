package com.floworchestrator.vehicledomain.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentMethodResponse(
        @JsonProperty("purchase_order_id") String purchaseOrderId,
        String status,
        @JsonProperty("payment_type") String paymentType) {
}
