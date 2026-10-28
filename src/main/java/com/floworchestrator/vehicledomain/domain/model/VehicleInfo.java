package com.floworchestrator.vehicledomain.domain.model;

import java.math.BigDecimal;

public record VehicleInfo(
        String vehicleId,
        String modelId,
        String brand,
        String model,
        Integer year,
        boolean available,
        BigDecimal price,
        String currency) {
}
