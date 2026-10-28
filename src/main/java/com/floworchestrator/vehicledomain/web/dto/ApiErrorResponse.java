package com.floworchestrator.vehicledomain.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

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
