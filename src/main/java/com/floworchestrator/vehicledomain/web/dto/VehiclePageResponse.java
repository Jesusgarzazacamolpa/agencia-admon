package com.floworchestrator.vehicledomain.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record VehiclePageResponse(
        List<VehicleResponse> vehicles,
        long total,
        int page,
        @JsonProperty("pageSize") int pageSize) {
}
