package com.floworchestrator.vehicledomain.web.controller;

import com.floworchestrator.vehicledomain.domain.model.PaymentRegistered;
import com.floworchestrator.vehicledomain.domain.model.PurchaseOrderCreated;
import com.floworchestrator.vehicledomain.domain.model.VehicleInfo;
import com.floworchestrator.vehicledomain.service.VehicleDomainService;
import com.floworchestrator.vehicledomain.web.dto.CreatePurchaseOrderRequest;
import com.floworchestrator.vehicledomain.web.dto.PaymentMethodResponse;
import com.floworchestrator.vehicledomain.web.dto.PurchaseOrderResponse;
import com.floworchestrator.vehicledomain.web.dto.RegisterPaymentRequest;
import com.floworchestrator.vehicledomain.web.dto.VehiclePageResponse;
import com.floworchestrator.vehicledomain.web.dto.VehicleResponse;
import com.floworchestrator.vehicledomain.web.mapper.VehicleWebMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST del dominio de vehículos y compras. No define prefijo común: expone
 * /health, /vehicles, /vehicles/available, /vehicles/{vehicleId}, /purchase-orders y /payment-methods.
 */
@RestController
@Validated
@RequiredArgsConstructor
public class VehicleDomainController {

    private static final int MIN_PAGE = 1;
    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MIN_YEAR = 1900;
    private static final int MAX_YEAR = 2100;

    private final VehicleDomainService vehicleDomainService;

    /**
     * GET /health: comprobación simple de disponibilidad que devuelve el texto fijo "Auto DB OK".
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auto DB OK");
    }

    /**
     * GET /vehicles: consulta el catálogo con filtros opcionales y paginación 1-based
     * (page por defecto 1 y size por defecto 20, entre 1 y 100).
     */
    @GetMapping("/vehicles")
    public ResponseEntity<VehiclePageResponse> getVehicles(
            @RequestParam(defaultValue = "1")
            @Min(value = MIN_PAGE, message = "La página debe ser mayor o igual a 1.")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = MIN_PAGE_SIZE, message = "El tamaño de página debe ser al menos 1.")
            @Max(value = MAX_PAGE_SIZE, message = "El tamaño de página no puede ser mayor que 100.")
            int size,

            @RequestParam(required = false)
            @Size(max = 100, message = "El filtro de marca no debe exceder 100 caracteres.")
            String brand,

            @RequestParam(required = false)
            @Size(max = 100, message = "El filtro de modelo no debe exceder 100 caracteres.")
            String model,

            @RequestParam(required = false) String status,

            @RequestParam(required = false)
            @DecimalMin(value = "0.0", message = "El precio mínimo no puede ser negativo.")
            BigDecimal minPrice,

            @RequestParam(required = false)
            @DecimalMin(value = "0.0", message = "El precio máximo no puede ser negativo.")
            BigDecimal maxPrice,

            @RequestParam(required = false)
            @Min(value = MIN_YEAR, message = "El año debe ser mayor o igual a 1900.")
            @Max(value = MAX_YEAR, message = "El año debe ser menor o igual a 2100.")
            Integer year) {
        return ResponseEntity.ok(VehicleWebMapper.toResponse(
                vehicleDomainService.getVehicles(page, size, brand, model, status, minPrice, maxPrice, year)));
    }

    /**
     * GET /vehicles/available: devuelve todos los vehículos disponibles sin paginar.
     */
    @GetMapping("/vehicles/available")
    public ResponseEntity<List<VehicleResponse>> getAvailableVehicles() {
        List<VehicleResponse> vehicles = vehicleDomainService.getAvailableVehicles().stream()
                .map(VehicleWebMapper::toResponse)
                .toList();
        return ResponseEntity.ok(vehicles);
    }

    /**
     * GET /vehicles/{vehicleId}: consulta un vehículo por su identificador; responde 404 si no existe.
     */
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<VehicleResponse> getVehicle(@PathVariable String vehicleId) {
        VehicleInfo vehicle = vehicleDomainService.getVehicleById(vehicleId);
        return ResponseEntity.ok(VehicleWebMapper.toResponse(vehicle));
    }

    /**
     * POST /purchase-orders: registra una orden de compra de forma idempotente y responde 200 OK
     * con el identificador, el estado y un mensaje.
     */
    @PostMapping(path = "/purchase-orders", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(
            @Valid @RequestBody CreatePurchaseOrderRequest request) {
        PurchaseOrderCreated order = vehicleDomainService.createPurchaseOrder(
                VehicleWebMapper.toCommand(request));
        return ResponseEntity.ok(VehicleWebMapper.toResponse(order));
    }

    /**
     * POST /payment-methods: registra el pago de una orden existente y la deja en estado
     * PAYMENT_CONFIGURED; responde 404 si la orden no existe.
     */
    @PostMapping(path = "/payment-methods", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentMethodResponse> registerPayment(
            @Valid @RequestBody RegisterPaymentRequest request) {
        PaymentRegistered payment = vehicleDomainService.registerPayment(
                VehicleWebMapper.toCommand(request));
        return ResponseEntity.ok(VehicleWebMapper.toResponse(payment));
    }
}
