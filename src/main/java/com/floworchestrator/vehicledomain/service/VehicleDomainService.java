package com.floworchestrator.vehicledomain.service;

import com.floworchestrator.vehicledomain.entity.PaymentMethod;
import com.floworchestrator.vehicledomain.entity.PurchaseOrder;
import com.floworchestrator.vehicledomain.entity.Vehicle;
import com.floworchestrator.vehicledomain.repository.PaymentMethodRepository;
import com.floworchestrator.vehicledomain.repository.PurchaseOrderRepository;
import com.floworchestrator.vehicledomain.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleDomainService {

    private final VehicleRepository vehicleRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    public Map<String, Object> getVehicleById(String vehicleId) {
        return vehicleRepository.findByVehicleId(vehicleId)
                .map(this::toVehicleMap)
                .orElseGet(() -> defaultVehicle(vehicleId));
    }

    public Map<String, Object> createPurchaseOrder(Map<String, Object> payload) {
        String purchaseOrderId = payload.containsKey("purchase_order_id")
                ? payload.get("purchase_order_id").toString()
                : "po-" + System.currentTimeMillis();

        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseOrderId(purchaseOrderId);
        order.setCustomerId(String.valueOf(payload.getOrDefault("customer_id", "customer-unknown")));
        order.setVehicleId(String.valueOf(payload.getOrDefault("vehicle_id", "model-101")));
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        purchaseOrderRepository.save(order);

        Map<String, Object> response = new HashMap<>();
        response.put("purchase_order_id", purchaseOrderId);
        response.put("status", "CREATED");
        response.put("message", "Orden registrada correctamente");
        return response;
    }

    public Map<String, Object> registerPayment(Map<String, Object> payload) {
        String purchaseOrderId = String.valueOf(payload.getOrDefault("purchase_order_id", "po-1001"));
        String paymentType = String.valueOf(payload.getOrDefault("payment_type", "FINANCING"));
        String financialInstitution = String.valueOf(payload.getOrDefault("financial_institution", "BBVA"));

        PaymentMethod payment = new PaymentMethod();
        payment.setPurchaseOrderId(purchaseOrderId);
        payment.setPaymentType(paymentType);
        payment.setFinancialInstitution(financialInstitution);
        payment.setStatus("PAYMENT_CONFIGURED");
        payment.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        paymentMethodRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("purchase_order_id", purchaseOrderId);
        response.put("status", "PAYMENT_CONFIGURED");
        response.put("payment_type", paymentType);
        return response;
    }

    private Map<String, Object> toVehicleMap(Vehicle vehicle) {
        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("vehicle_id", vehicle.getVehicleId());
        vehicleMap.put("model_id", vehicle.getModelId());
        vehicleMap.put("brand", vehicle.getBrand());
        vehicleMap.put("model", vehicle.getModel());
        vehicleMap.put("available", vehicle.isAvailable());
        vehicleMap.put("price", vehicle.getPrice());
        vehicleMap.put("currency", vehicle.getCurrency());
        return vehicleMap;
    }

    private Map<String, Object> defaultVehicle(String vehicleId) {
        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("vehicle_id", vehicleId != null ? vehicleId : "model-101");
        vehicleMap.put("model_id", "model-101");
        vehicleMap.put("brand", "Toyota");
        vehicleMap.put("model", "Corolla");
        vehicleMap.put("available", true);
        vehicleMap.put("price", new BigDecimal("28900.00"));
        vehicleMap.put("currency", "MXN");
        return vehicleMap;
    }
}
