package com.floworchestrator.vehicledomain.web.mapper;

import com.floworchestrator.vehicledomain.domain.model.CreatePurchaseOrderCommand;
import com.floworchestrator.vehicledomain.domain.model.PaymentRegistered;
import com.floworchestrator.vehicledomain.domain.model.PurchaseOrderCreated;
import com.floworchestrator.vehicledomain.domain.model.RegisterPaymentCommand;
import com.floworchestrator.vehicledomain.domain.model.VehicleInfo;
import com.floworchestrator.vehicledomain.domain.model.VehiclePage;
import com.floworchestrator.vehicledomain.web.dto.CreatePurchaseOrderRequest;
import com.floworchestrator.vehicledomain.web.dto.PaymentMethodResponse;
import com.floworchestrator.vehicledomain.web.dto.PurchaseOrderResponse;
import com.floworchestrator.vehicledomain.web.dto.RegisterPaymentRequest;
import com.floworchestrator.vehicledomain.web.dto.VehiclePageResponse;
import com.floworchestrator.vehicledomain.web.dto.VehicleResponse;

/**
 * Mapper estático entre los DTOs web y el modelo de dominio.
 * Centraliza las conversiones de entrada (comandos) y salida (respuestas) de la API.
 */
public final class VehicleWebMapper {

    private VehicleWebMapper() {
    }

    public static VehicleResponse toResponse(VehicleInfo vehicle) {
        return new VehicleResponse(
                vehicle.vehicleId(),
                vehicle.modelId(),
                vehicle.brand(),
                vehicle.model(),
                vehicle.year(),
                vehicle.available(),
                vehicle.price(),
                vehicle.currency());
    }

    public static VehiclePageResponse toResponse(VehiclePage page) {
        return new VehiclePageResponse(
                page.vehicles().stream().map(VehicleWebMapper::toResponse).toList(),
                page.total(),
                page.page(),
                page.pageSize());
    }

    public static PurchaseOrderResponse toResponse(PurchaseOrderCreated order) {
        return new PurchaseOrderResponse(order.purchaseOrderId(), order.status().name(), order.message());
    }

    public static PaymentMethodResponse toResponse(PaymentRegistered payment) {
        return new PaymentMethodResponse(payment.purchaseOrderId(), payment.status().name(), payment.paymentType());
    }

    public static CreatePurchaseOrderCommand toCommand(CreatePurchaseOrderRequest request) {
        return new CreatePurchaseOrderCommand(request.vehicleId(), request.customerId(), request.purchaseOrderId());
    }

    public static RegisterPaymentCommand toCommand(RegisterPaymentRequest request) {
        return new RegisterPaymentCommand(
                request.purchaseOrderId(),
                request.paymentType(),
                request.financialInstitution());
    }
}
