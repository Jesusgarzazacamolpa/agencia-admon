package com.floworchestrator.vehicledomain.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Contrato de error HTTP de la API: estado, mensaje, código funcional (flow_code),
 * ruta, identificador de traza y detalles de validación por campo.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        @JsonProperty("flow_code") String flowCode,
        String path,
        @JsonProperty("trace_id") String traceId,
        List<FieldValidationError> details) {
}
