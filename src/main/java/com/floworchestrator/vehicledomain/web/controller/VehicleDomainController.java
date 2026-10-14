package com.floworchestrator.vehicledomain.web.controller;

import com.floworchestrator.vehicledomain.service.VehicleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VehicleDomainController {

    private final VehicleDomainService vehicleDomainService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auto DB OK");
    }

    @GetMapping("/vehicles")
    public ResponseEntity<Map<String, Object>> getVehicles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(vehicleDomainService.getVehicles(page, size, brand, model, status, minPrice, maxPrice, year));
    }

    @GetMapping("/vehicles/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableVehicles() {
        return ResponseEntity.ok(vehicleDomainService.getAvailableVehicles());
    }

    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<Map<String, Object>> getVehicle(@PathVariable String vehicleId) {
        return ResponseEntity.ok(vehicleDomainService.getVehicleById(vehicleId));
    }

    @PostMapping("/purchase-orders")
    public ResponseEntity<Map<String, Object>> createPurchaseOrder(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(vehicleDomainService.createPurchaseOrder(payload));
    }

    @PostMapping("/payment-methods")
    public ResponseEntity<Map<String, Object>> registerPayment(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(vehicleDomainService.registerPayment(payload));
    }
}
