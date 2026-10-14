package com.floworchestrator.vehicledomain.service;

import com.floworchestrator.vehicledomain.entity.PaymentMethod;
import com.floworchestrator.vehicledomain.entity.PurchaseOrder;
import com.floworchestrator.vehicledomain.entity.Vehicle;
import com.floworchestrator.vehicledomain.repository.PaymentMethodRepository;
import com.floworchestrator.vehicledomain.repository.PurchaseOrderRepository;
import com.floworchestrator.vehicledomain.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleDomainService {

    private static final String STATUS_CREATED = "CREATED";
    private static final String STATUS_PAYMENT_CONFIGURED = "PAYMENT_CONFIGURED";
    private static final String PURCHASE_ORDER_ID_PREFIX = "po-";

    private final VehicleRepository vehicleRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    public Map<String, Object> getVehicleById(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found: " + vehicleId));
        return toVehicleMap(vehicle);
    }

    public Map<String, Object> getVehicles(int page, int size, String brand, String model, String status,
                                           BigDecimal minPrice, BigDecimal maxPrice, Integer year) {
        Pageable pageable = PageRequest.of(page - 1, size);
        
        Boolean available = null;
        if (status != null) {
            available = "AVAILABLE".equalsIgnoreCase(status);
        }

        Page<Vehicle> vehiclePage = vehicleRepository.findWithFilters(
                brand, model, available, minPrice, maxPrice, pageable);

        List<Map<String, Object>> vehicles = vehiclePage.getContent().stream()
                .map(this::toVehicleMap)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("vehicles", vehicles);
        response.put("total", vehiclePage.getTotalElements());
        response.put("page", page);
        response.put("pageSize", size);
        return response;
    }

    public List<Map<String, Object>> getAvailableVehicles() {
        return vehicleRepository.findByAvailableTrue().stream()
                .map(this::toVehicleMap)
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> createPurchaseOrder(Map<String, Object> payload) {
        String vehicleId = getRequiredField(payload, "vehicle_id");
        String customerId = getRequiredField(payload, "customer_id");

        Object purchaseOrderIdValue = payload.get("purchase_order_id");
        String purchaseOrderId = purchaseOrderIdValue != null && !purchaseOrderIdValue.toString().isBlank()
                ? purchaseOrderIdValue.toString()
                : PURCHASE_ORDER_ID_PREFIX + UUID.randomUUID();

        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseOrderId(purchaseOrderId);
        order.setCustomerId(customerId);
        order.setVehicleId(vehicleId);
        order.setStatus(STATUS_CREATED);
        order.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        purchaseOrderRepository.save(order);

        Map<String, Object> response = new HashMap<>();
        response.put("purchase_order_id", purchaseOrderId);
        response.put("status", STATUS_CREATED);
        response.put("message", "Orden registrada correctamente");
        return response;
    }

    @Transactional
    public Map<String, Object> registerPayment(Map<String, Object> payload) {
        String purchaseOrderId = getRequiredField(payload, "purchase_order_id");
        String paymentType = getRequiredField(payload, "payment_type");
        String financialInstitution = getRequiredField(payload, "financial_institution");

        PaymentMethod payment = new PaymentMethod();
        payment.setPurchaseOrderId(purchaseOrderId);
        payment.setPaymentType(paymentType);
        payment.setFinancialInstitution(financialInstitution);
        payment.setStatus(STATUS_PAYMENT_CONFIGURED);
        payment.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        paymentMethodRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("purchase_order_id", purchaseOrderId);
        response.put("status", STATUS_PAYMENT_CONFIGURED);
        response.put("payment_type", paymentType);
        return response;
    }

    private String getRequiredField(Map<String, Object> payload, String fieldName) {
        Object value = payload.get(fieldName);
        if (value == null || value.toString().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required field is missing: " + fieldName);
        }
        return value.toString();
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
}
