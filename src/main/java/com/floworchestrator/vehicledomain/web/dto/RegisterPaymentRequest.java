package com.floworchestrator.vehicledomain.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la petición para registrar un pago sobre una orden de compra existente.
 * El tipo de pago y la institución financiera se almacenan como texto libre.
 */
public record RegisterPaymentRequest(
        @JsonProperty("purchase_order_id")
        @NotBlank(message = "El identificador de la orden de compra es obligatorio.")
        @Size(max = 100, message = "El identificador de la orden no debe exceder 100 caracteres.")
        String purchaseOrderId,

        @JsonProperty("payment_type")
        @NotBlank(message = "El tipo de pago es obligatorio.")
        @Size(max = 50, message = "El tipo de pago no debe exceder 50 caracteres.")
        String paymentType,

        @JsonProperty("financial_institution")
        @NotBlank(message = "La institución financiera es obligatoria.")
        @Size(max = 150, message = "La institución financiera no debe exceder 150 caracteres.")
        String financialInstitution) {
}
