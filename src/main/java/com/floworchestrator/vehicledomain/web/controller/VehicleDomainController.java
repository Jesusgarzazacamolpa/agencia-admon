package com.floworchestrator.vehicledomain.web.controller;

import com.floworchestrator.vehicledomain.service.VehicleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class VehicleDomainController {

    private final VehicleDomainService vehicleDomainService;

    @GetMapping("/health")
    public String health() {
        return "Auto DB OK";
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
