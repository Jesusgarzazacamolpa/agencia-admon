package com.floworchestrator.vehicledomain.domain.model;

import java.util.List;

public record VehiclePage(List<VehicleInfo> vehicles, long total, int page, int pageSize) {

    public VehiclePage {
        vehicles = List.copyOf(vehicles);
    }
}
