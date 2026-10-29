package com.floworchestrator.vehicledomain.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Respuesta paginada del catálogo de vehículos; page es 1-based y pageSize refleja el tamaño solicitado.
 */
public record VehiclePageResponse(
        List<VehicleResponse> vehicles,
        long total,
        int page,
        @JsonProperty("pageSize") int pageSize) {
}
