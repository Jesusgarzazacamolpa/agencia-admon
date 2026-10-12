package com.floworchestrator.vehicledomain.repository;

import com.floworchestrator.vehicledomain.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    Optional<Vehicle> findByVehicleId(String vehicleId);
}
