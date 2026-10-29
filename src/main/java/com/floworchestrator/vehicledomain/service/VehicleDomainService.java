package com.floworchestrator.vehicledomain.service;

import com.floworchestrator.vehicledomain.domain.exception.ApiException;
import com.floworchestrator.vehicledomain.domain.exception.ErrorCode;
import com.floworchestrator.vehicledomain.domain.model.CreatePurchaseOrderCommand;
import com.floworchestrator.vehicledomain.domain.model.PaymentRegistered;
import com.floworchestrator.vehicledomain.domain.model.PaymentStatus;
import com.floworchestrator.vehicledomain.domain.model.PurchaseOrderCreated;
import com.floworchestrator.vehicledomain.domain.model.PurchaseOrderStatus;
import com.floworchestrator.vehicledomain.domain.model.RegisterPaymentCommand;
import com.floworchestrator.vehicledomain.domain.model.VehicleInfo;
import com.floworchestrator.vehicledomain.domain.model.VehiclePage;
import com.floworchestrator.vehicledomain.entity.PaymentMethod;
import com.floworchestrator.vehicledomain.entity.PurchaseOrder;
import com.floworchestrator.vehicledomain.entity.Vehicle;
import com.floworchestrator.vehicledomain.repository.PaymentMethodRepository;
import com.floworchestrator.vehicledomain.repository.PurchaseOrderRepository;
import com.floworchestrator.vehicledomain.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de dominio de vehículos y compras. Centraliza las reglas de consulta del
 * catálogo, el alta idempotente de órdenes de compra y el registro de pagos.
 */
@Service
@RequiredArgsConstructor
public class VehicleDomainService {

    private static final Logger log = LoggerFactory.getLogger(VehicleDomainService.class);

    private static final String PURCHASE_ORDER_ID_PREFIX = "po-";
    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_UNAVAILABLE = "UNAVAILABLE";
    private static final String PURCHASE_ORDER_CREATED_MESSAGE = "Orden registrada correctamente";
    private static final String PURCHASE_ORDER_REPLAYED_MESSAGE =
            "La orden ya estaba registrada; se reutiliza la existente";
    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("brand"),
            Sort.Order.asc("model"),
            Sort.Order.asc("vehicleId"));

    private final VehicleRepository vehicleRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    /**
     * Obtiene un vehículo por su identificador de negocio.
     *
     * @throws ApiException con VEHICLE_NOT_FOUND si no existe
     */
    @Transactional(readOnly = true)
    public VehicleInfo getVehicleById(String vehicleId) {
        return vehicleRepository.findByVehicleId(vehicleId)
                .map(VehicleDomainService::toVehicleInfo)
                .orElseThrow(() -> new ApiException(ErrorCode.VEHICLE_NOT_FOUND));
    }

    /**
     * Consulta el catálogo con filtros opcionales y paginación basada en 1, ordenado por
     * marca, modelo e identificador; el estado solo admite AVAILABLE o UNAVAILABLE.
     *
     * @param page número de página, empezando en 1
     * @throws ApiException INVALID_PRICE_RANGE si el mínimo supera al máximo, o INVALID_PARAMETER si el estado no es válido
     */
    @Transactional(readOnly = true)
    public VehiclePage getVehicles(int page, int size, String brand, String model, String status,
                                   BigDecimal minPrice, BigDecimal maxPrice, Integer year) {
        validatePriceRange(minPrice, maxPrice);

        Pageable pageable = PageRequest.of(page - 1, size, DEFAULT_SORT);
        Page<Vehicle> vehicles = vehicleRepository.findWithFilters(
                trimToNull(brand),
                trimToNull(model),
                resolveAvailability(status),
                minPrice,
                maxPrice,
                year,
                pageable);

        List<VehicleInfo> content = vehicles.getContent().stream()
                .map(VehicleDomainService::toVehicleInfo)
                .toList();

        return new VehiclePage(content, vehicles.getTotalElements(), page, size);
    }

    /**
     * Devuelve todos los vehículos disponibles sin paginar.
     */
    @Transactional(readOnly = true)
    public List<VehicleInfo> getAvailableVehicles() {
        return vehicleRepository.findByAvailableTrue().stream()
                .map(VehicleDomainService::toVehicleInfo)
                .toList();
    }

    /**
     * Registra una orden de compra de forma idempotente por purchase_order_id: si la orden
     * ya existe con el mismo vehículo y cliente la reutiliza, y si difieren lanza conflicto.
     *
     * @throws ApiException VEHICLE_NOT_FOUND si el vehículo no existe, o DATA_CONFLICT si el identificador pertenece a otra combinación
     */
    @Transactional
    public PurchaseOrderCreated createPurchaseOrder(CreatePurchaseOrderCommand command) {
        vehicleRepository.findByVehicleId(command.vehicleId())
                .orElseThrow(() -> new ApiException(ErrorCode.VEHICLE_NOT_FOUND));

        String purchaseOrderId = resolvePurchaseOrderId(command.purchaseOrderId());
        Optional<PurchaseOrder> existing =
                purchaseOrderRepository.findByPurchaseOrderId(purchaseOrderId);
        if (existing.isPresent()) {
            PurchaseOrder order = existing.get();
            if (!order.getVehicleId().equals(command.vehicleId())
                    || !order.getCustomerId().equals(command.customerId())) {
                throw new ApiException(ErrorCode.DATA_CONFLICT,
                        "El identificador de orden ya está asociado a otro vehículo o cliente.");
            }
            log.info("purchase_order_replayed purchaseOrderId={} vehicleId={}",
                    purchaseOrderId, command.vehicleId());
            return new PurchaseOrderCreated(purchaseOrderId, order.getStatus(),
                    PURCHASE_ORDER_REPLAYED_MESSAGE);
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseOrderId(purchaseOrderId);
        order.setCustomerId(command.customerId());
        order.setVehicleId(command.vehicleId());
        order.setStatus(PurchaseOrderStatus.CREATED);
        purchaseOrderRepository.save(order);

        log.info("purchase_order_created purchaseOrderId={} vehicleId={}", purchaseOrderId, command.vehicleId());
        return new PurchaseOrderCreated(purchaseOrderId, PurchaseOrderStatus.CREATED, PURCHASE_ORDER_CREATED_MESSAGE);
    }

    /**
     * Registra un pago configurado para una orden existente y la marca como PAYMENT_CONFIGURED
     * dentro de la misma transacción; no valida el tipo de pago contra un catálogo.
     *
     * @throws ApiException PURCHASE_ORDER_NOT_FOUND si la orden no existe
     */
    @Transactional
    public PaymentRegistered registerPayment(RegisterPaymentCommand command) {
        PurchaseOrder order = purchaseOrderRepository.findByPurchaseOrderId(command.purchaseOrderId())
                .orElseThrow(() -> new ApiException(ErrorCode.PURCHASE_ORDER_NOT_FOUND));

        PaymentMethod payment = new PaymentMethod();
        payment.setPurchaseOrderId(command.purchaseOrderId());
        payment.setPaymentType(command.paymentType());
        payment.setFinancialInstitution(command.financialInstitution());
        payment.setStatus(PaymentStatus.PAYMENT_CONFIGURED);
        paymentMethodRepository.save(payment);

        order.setStatus(PurchaseOrderStatus.PAYMENT_CONFIGURED);

        log.info("payment_registered purchaseOrderId={} paymentType={}",
                command.purchaseOrderId(), command.paymentType());
        return new PaymentRegistered(command.purchaseOrderId(), PaymentStatus.PAYMENT_CONFIGURED, command.paymentType());
    }

    /**
     * Rechaza el rango cuando ambos límites están informados y el mínimo supera al máximo.
     */
    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ApiException(ErrorCode.INVALID_PRICE_RANGE);
        }
    }

    /**
     * Traduce AVAILABLE/UNAVAILABLE a un booleano y rechaza cualquier otro valor.
     */
    private Boolean resolveAvailability(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case STATUS_AVAILABLE -> true;
            case STATUS_UNAVAILABLE -> false;
            default -> throw new ApiException(ErrorCode.INVALID_PARAMETER,
                    "El estado '" + status + "' no es válido. Usa AVAILABLE o UNAVAILABLE.");
        };
    }

    /**
     * Usa el identificador informado o genera uno nuevo con el prefijo "po-".
     */
    private String resolvePurchaseOrderId(String providedId) {
        if (providedId == null || providedId.isBlank()) {
            return PURCHASE_ORDER_ID_PREFIX + UUID.randomUUID();
        }
        return providedId.trim();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static VehicleInfo toVehicleInfo(Vehicle vehicle) {
        return new VehicleInfo(
                vehicle.getVehicleId(),
                vehicle.getModelId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.isAvailable(),
                vehicle.getPrice(),
                vehicle.getCurrency());
    }
}
