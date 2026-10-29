package com.floworchestrator.vehicledomain.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la petición para registrar una orden de compra. El purchase_order_id es
 * opcional; si se envía, solo admite letras, números, guiones y guiones bajos.
 */
public record CreatePurchaseOrderRequest(
        @JsonProperty("vehicle_id")
        @NotBlank(message = "El identificador del vehículo es obligatorio.")
        @Size(max = 100, message = "El identificador del vehículo no debe exceder 100 caracteres.")
        String vehicleId,

        @JsonProperty("customer_id")
        @NotBlank(message = "El identificador del cliente es obligatorio.")
        @Size(max = 100, message = "El identificador del cliente no debe exceder 100 caracteres.")
        String customerId,

        @JsonProperty("purchase_order_id")
        @Size(max = 100, message = "El identificador de la orden no debe exceder 100 caracteres.")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$",
                message = "El identificador de la orden solo admite letras, números, guiones y guiones bajos.")
        String purchaseOrderId) {
}
