package com.floworchestrator.vehicledomain.service;

import com.floworchestrator.vehicledomain.domain.exception.ApiException;
import com.floworchestrator.vehicledomain.domain.exception.ErrorCode;
import com.floworchestrator.vehicledomain.domain.model.CreatePurchaseOrderCommand;
import com.floworchestrator.vehicledomain.domain.model.PaymentRegistered;
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
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleDomainServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private VehicleDomainService service;

    @Test
    void shouldReturnVehicleInfoWhenVehicleExists() {
        when(vehicleRepository.findByVehicleId("V-001")).thenReturn(Optional.of(vehicle("V-001", "Toyota", "Corolla")));

        VehicleInfo result = service.getVehicleById("V-001");

        assertThat(result.vehicleId()).isEqualTo("V-001");
        assertThat(result.modelId()).isEqualTo("model-V-001");
        assertThat(result.brand()).isEqualTo("Toyota");
        assertThat(result.model()).isEqualTo("Corolla");
        assertThat(result.year()).isEqualTo(2024);
        assertThat(result.available()).isTrue();
        assertThat(result.price()).isEqualByComparingTo("28900.00");
        assertThat(result.currency()).isEqualTo("MXN");
    }

    @Test
    void shouldThrowNotFoundWhenVehicleDoesNotExist() {
        when(vehicleRepository.findByVehicleId("unknown")).thenReturn(Optional.empty());

        assertApiException(() -> service.getVehicleById("unknown"), ErrorCode.VEHICLE_NOT_FOUND);
    }

    @Test
    void shouldReturnFilteredPageWithNormalizedParameters() {
        Page<Vehicle> page = new PageImpl<>(List.of(vehicle("V-001", "Toyota", "Corolla")),
                PageRequest.of(0, 20), 1);
        BigDecimal minPrice = new BigDecimal("100000.00");

        when(vehicleRepository.findWithFilters(eq("Toyota"), isNull(), eq(true), eq(minPrice),
                isNull(), eq(2024), any(Pageable.class))).thenReturn(page);

        VehiclePage result = service.getVehicles(1, 20, "  Toyota  ", null, "available", minPrice, null, 2024);

        assertThat(result.vehicles()).hasSize(1);
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(20);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(vehicleRepository).findWithFilters(eq("Toyota"), isNull(), eq(true), eq(minPrice),
                isNull(), eq(2024), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("brand")).isNotNull();
    }

    @Test
    void shouldResolveUnavailableStatus() {
        when(vehicleRepository.findWithFilters(isNull(), isNull(), eq(false), isNull(), isNull(), isNull(),
                any(Pageable.class))).thenReturn(Page.empty());

        service.getVehicles(1, 10, null, null, "UNAVAILABLE", null, null, null);

        verify(vehicleRepository).findWithFilters(isNull(), isNull(), eq(false), isNull(), isNull(), isNull(),
                any(Pageable.class));
    }

    @Test
    void shouldRejectUnknownStatusWithInvalidParameter() {
        assertApiException(() -> service.getVehicles(1, 20, null, null, "SOLD", null, null, null),
                ErrorCode.INVALID_PARAMETER);

        verifyNoInteractions(vehicleRepository);
    }

    @Test
    void shouldRejectPriceRangeWhenMinimumIsGreaterThanMaximum() {
        assertApiException(() -> service.getVehicles(1, 20, null, null, null,
                        new BigDecimal("500000"), new BigDecimal("100000"), null),
                ErrorCode.INVALID_PRICE_RANGE);

        verifyNoInteractions(vehicleRepository);
    }

    @Test
    void shouldReturnAvailableVehicles() {
        when(vehicleRepository.findByAvailableTrue())
                .thenReturn(List.of(vehicle("V-001", "Toyota", "Corolla"), vehicle("V-002", "Mazda", "Mazda 3")));

        List<VehicleInfo> result = service.getAvailableVehicles();

        assertThat(result).extracting(VehicleInfo::vehicleId).containsExactly("V-001", "V-002");
    }

    @Test
    void shouldCreatePurchaseOrderWithAutoGeneratedId() {
        when(vehicleRepository.findByVehicleId("V-001")).thenReturn(Optional.of(vehicle("V-001", "Toyota", "Corolla")));
        when(purchaseOrderRepository.findByPurchaseOrderId(any())).thenReturn(Optional.empty());

        PurchaseOrderCreated result = service.createPurchaseOrder(
                new CreatePurchaseOrderCommand("V-001", "customer-007", null));

        ArgumentCaptor<PurchaseOrder> captor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(purchaseOrderRepository).save(captor.capture());
        PurchaseOrder saved = captor.getValue();

        assertThat(saved.getPurchaseOrderId()).startsWith("po-");
        assertThat(saved.getCustomerId()).isEqualTo("customer-007");
        assertThat(saved.getVehicleId()).isEqualTo("V-001");
        assertThat(saved.getStatus()).isEqualTo(PurchaseOrderStatus.CREATED);

        assertThat(result.purchaseOrderId()).isEqualTo(saved.getPurchaseOrderId());
        assertThat(result.status()).isEqualTo(PurchaseOrderStatus.CREATED);
        assertThat(result.message()).isEqualTo("Orden registrada correctamente");
    }

    @Test
    void shouldCreatePurchaseOrderWithProvidedIdTrimmed() {
        when(vehicleRepository.findByVehicleId("V-999")).thenReturn(Optional.of(vehicle("V-999", "Kia", "Forte")));
        when(purchaseOrderRepository.findByPurchaseOrderId("po-custom-001")).thenReturn(Optional.empty());

        PurchaseOrderCreated result = service.createPurchaseOrder(
                new CreatePurchaseOrderCommand("V-999", "customer-007", "  po-custom-001  "));

        ArgumentCaptor<PurchaseOrder> captor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(purchaseOrderRepository).save(captor.capture());
        assertThat(captor.getValue().getPurchaseOrderId()).isEqualTo("po-custom-001");
        assertThat(result.purchaseOrderId()).isEqualTo("po-custom-001");
    }

    @Test
    void shouldRejectPurchaseOrderWhenVehicleDoesNotExist() {
        when(vehicleRepository.findByVehicleId("V-404")).thenReturn(Optional.empty());

        assertApiException(() -> service.createPurchaseOrder(
                new CreatePurchaseOrderCommand("V-404", "customer-007", null)), ErrorCode.VEHICLE_NOT_FOUND);

        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    void shouldReuseExistingPurchaseOrderOnReplay() {
        when(vehicleRepository.findByVehicleId("V-001")).thenReturn(Optional.of(vehicle("V-001", "Toyota", "Corolla")));
        when(purchaseOrderRepository.findByPurchaseOrderId("po-001"))
                .thenReturn(Optional.of(purchaseOrder("po-001")));

        PurchaseOrderCreated result = service.createPurchaseOrder(
                new CreatePurchaseOrderCommand("V-001", "customer-007", "po-001"));

        assertThat(result.purchaseOrderId()).isEqualTo("po-001");
        assertThat(result.status()).isEqualTo(PurchaseOrderStatus.CREATED);
        assertThat(result.message()).contains("ya estaba registrada");
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    void shouldRejectReplayWhenExistingOrderBelongsToAnotherCustomer() {
        PurchaseOrder existing = purchaseOrder("po-001");
        existing.setCustomerId("customer-999");
        when(vehicleRepository.findByVehicleId("V-001")).thenReturn(Optional.of(vehicle("V-001", "Toyota", "Corolla")));
        when(purchaseOrderRepository.findByPurchaseOrderId("po-001")).thenReturn(Optional.of(existing));

        assertApiException(() -> service.createPurchaseOrder(
                        new CreatePurchaseOrderCommand("V-001", "customer-007", "po-001")),
                ErrorCode.DATA_CONFLICT);

        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    void shouldRegisterPaymentAndMoveOrderToPaymentConfigured() {
        PurchaseOrder order = purchaseOrder("po-555");
        when(purchaseOrderRepository.findByPurchaseOrderId("po-555")).thenReturn(Optional.of(order));

        PaymentRegistered result = service.registerPayment(
                new RegisterPaymentCommand("po-555", "CASH", "Santander"));

        ArgumentCaptor<PaymentMethod> captor = ArgumentCaptor.forClass(PaymentMethod.class);
        verify(paymentMethodRepository).save(captor.capture());
        PaymentMethod saved = captor.getValue();

        assertThat(saved.getPurchaseOrderId()).isEqualTo("po-555");
        assertThat(saved.getPaymentType()).isEqualTo("CASH");
        assertThat(saved.getFinancialInstitution()).isEqualTo("Santander");
        assertThat(saved.getStatus().name()).isEqualTo("PAYMENT_CONFIGURED");

        assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.PAYMENT_CONFIGURED);
        assertThat(result.purchaseOrderId()).isEqualTo("po-555");
        assertThat(result.status().name()).isEqualTo("PAYMENT_CONFIGURED");
        assertThat(result.paymentType()).isEqualTo("CASH");
    }

    @Test
    void shouldRejectPaymentWhenPurchaseOrderDoesNotExist() {
        when(purchaseOrderRepository.findByPurchaseOrderId("po-404")).thenReturn(Optional.empty());

        assertApiException(() -> service.registerPayment(
                new RegisterPaymentCommand("po-404", "CASH", "Santander")), ErrorCode.PURCHASE_ORDER_NOT_FOUND);

        verify(paymentMethodRepository, never()).save(any());
    }

    @Test
    void shouldSavePaymentMethodExactlyOnce() {
        when(purchaseOrderRepository.findByPurchaseOrderId("po-555"))
                .thenReturn(Optional.of(purchaseOrder("po-555")));

        service.registerPayment(new RegisterPaymentCommand("po-555", "CASH", "Santander"));

        verify(paymentMethodRepository, times(1)).save(any(PaymentMethod.class));
    }

    private static void assertApiException(ThrowingCallable callable, ErrorCode expectedCode) {
        assertThatThrownBy(callable)
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getErrorCode()).isEqualTo(expectedCode));
    }

    private static Vehicle vehicle(String vehicleId, String brand, String model) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(vehicleId);
        vehicle.setModelId("model-" + vehicleId);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setYear(2024);
        vehicle.setPrice(new BigDecimal("28900.00"));
        vehicle.setCurrency("MXN");
        vehicle.setAvailable(true);
        return vehicle;
    }

    private static PurchaseOrder purchaseOrder(String purchaseOrderId) {
        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseOrderId(purchaseOrderId);
        order.setCustomerId("customer-007");
        order.setVehicleId("V-001");
        order.setStatus(PurchaseOrderStatus.CREATED);
        return order;
    }
}
