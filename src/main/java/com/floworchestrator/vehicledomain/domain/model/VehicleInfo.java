package com.floworchestrator.vehicledomain.domain.model;

import java.math.BigDecimal;

/**
 * Representación de dominio de un vehículo del catálogo, con su disponibilidad y precio.
 */
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
