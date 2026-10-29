package com.floworchestrator.vehicledomain.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Representación HTTP de un vehículo del catálogo; los campos nulos se omiten en la respuesta.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VehicleResponse(
        @JsonProperty("vehicle_id") String vehicleId,
        @JsonProperty("model_id") String modelId,
        String brand,
        String model,
        Integer year,
        boolean available,
        BigDecimal price,
        String currency) {
}
