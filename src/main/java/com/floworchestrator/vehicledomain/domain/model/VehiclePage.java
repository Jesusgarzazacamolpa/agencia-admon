package com.floworchestrator.vehicledomain.domain.model;

import java.util.List;

/**
 * Página de resultados del catálogo de vehículos: contenido, total de coincidencias y datos de paginación.
 */
public record VehiclePage(List<VehicleInfo> vehicles, long total, int page, int pageSize) {

    /**
     * Copia defensiva de la lista para que la página quede inmutable.
     */
    public VehiclePage {
        vehicles = List.copyOf(vehicles);
    }
}
