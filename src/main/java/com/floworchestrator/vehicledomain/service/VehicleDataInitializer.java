package com.floworchestrator.vehicledomain.service;

import com.floworchestrator.vehicledomain.entity.Vehicle;
import com.floworchestrator.vehicledomain.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class VehicleDataInitializer implements CommandLineRunner {

    private final VehicleRepository vehicleRepository;

    @Override
    public void run(String... args) {
        if (vehicleRepository.findByVehicleId("model-101").isEmpty()) {
            Vehicle vehicle = new Vehicle();
            vehicle.setVehicleId("model-101");
            vehicle.setModelId("model-101");
            vehicle.setBrand("Toyota");
            vehicle.setModel("Corolla");
            vehicle.setPrice(new BigDecimal("28900.00"));
            vehicle.setCurrency("MXN");
            vehicle.setAvailable(true);
            vehicleRepository.save(vehicle);
        }
    }
}
