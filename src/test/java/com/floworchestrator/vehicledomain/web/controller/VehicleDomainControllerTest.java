package com.floworchestrator.vehicledomain.web.controller;

import com.floworchestrator.vehicledomain.domain.exception.ApiException;
import com.floworchestrator.vehicledomain.domain.exception.ErrorCode;
import com.floworchestrator.vehicledomain.domain.model.CreatePurchaseOrderCommand;
import com.floworchestrator.vehicledomain.domain.model.PaymentRegistered;
import com.floworchestrator.vehicledomain.domain.model.PaymentStatus;
import com.floworchestrator.vehicledomain.domain.model.PurchaseOrderCreated;
import com.floworchestrator.vehicledomain.domain.model.PurchaseOrderStatus;
import com.floworchestrator.vehicledomain.domain.model.VehicleInfo;
import com.floworchestrator.vehicledomain.domain.model.VehiclePage;
import com.floworchestrator.vehicledomain.service.VehicleDomainService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleDomainController.class)
class VehicleDomainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleDomainService vehicleDomainService;

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldReturnOkForHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Auto DB OK"));
    }

    @Test
    void shouldReturnVehicleById() throws Exception {
        when(vehicleDomainService.getVehicleById("V-001")).thenReturn(vehicleInfo());

        mockMvc.perform(get("/vehicles/V-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicle_id").value("V-001"))
                .andExpect(jsonPath("$.model_id").value("M-001"))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.price").value(28900.00));
    }

    @Test
    void shouldReturnPaginatedVehicles() throws Exception {
        when(vehicleDomainService.getVehicles(1, 20, "Toyota", null, "AVAILABLE",
                new BigDecimal("1000"), new BigDecimal("50000"), 2024))
                .thenReturn(new VehiclePage(List.of(vehicleInfo()), 1, 1, 20));

        mockMvc.perform(get("/vehicles")
                        .param("brand", "Toyota")
                        .param("status", "AVAILABLE")
                        .param("minPrice", "1000")
                        .param("maxPrice", "50000")
                        .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicles[0].vehicle_id").value("V-001"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    void shouldReturnAvailableVehicles() throws Exception {
        when(vehicleDomainService.getAvailableVehicles()).thenReturn(List.of(vehicleInfo()));

        mockMvc.perform(get("/vehicles/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vehicle_id").value("V-001"))
                .andExpect(jsonPath("$[0].brand").value("Toyota"));
    }

    @Test
    void shouldReturnNotFoundWithFlowCodeAndTraceId() throws Exception {
        MDC.put("traceId", "trace-123");
        when(vehicleDomainService.getVehicleById("unknown"))
                .thenThrow(new ApiException(ErrorCode.VEHICLE_NOT_FOUND));

        mockMvc.perform(get("/vehicles/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.flow_code").value("ERR_VEHICLE_NOT_FOUND_01"))
                .andExpect(jsonPath("$.message").value(ErrorCode.VEHICLE_NOT_FOUND.message()))
                .andExpect(jsonPath("$.path").value("/vehicles/unknown"))
                .andExpect(jsonPath("$.trace_id").value("trace-123"));
    }

    @Test
    void shouldRejectPageLowerThanOne() throws Exception {
        mockMvc.perform(get("/vehicles").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flow_code").value("ERR_INVALID_PARAM_01"))
                .andExpect(jsonPath("$.details[*].field", hasItem("page")));

        verifyNoInteractions(vehicleDomainService);
    }

    @Test
    void shouldRejectPageSizeGreaterThanMaximum() throws Exception {
        mockMvc.perform(get("/vehicles").param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flow_code").value("ERR_INVALID_PARAM_01"))
                .andExpect(jsonPath("$.details[*].field", hasItem("size")));

        verifyNoInteractions(vehicleDomainService);
    }

    @Test
    void shouldRejectParameterWithInvalidFormat() throws Exception {
        mockMvc.perform(get("/vehicles").param("minPrice", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flow_code").value("ERR_INVALID_PARAM_01"))
                .andExpect(jsonPath("$.message").value("El parámetro 'minPrice' tiene un formato incorrecto. "
                        + "Verifica el valor enviado."))
                .andExpect(jsonPath("$.details[0].field").value("minPrice"));
    }

    @Test
    void shouldCreatePurchaseOrder() throws Exception {
        when(vehicleDomainService.createPurchaseOrder(any())).thenReturn(new PurchaseOrderCreated(
                "po-test-001", PurchaseOrderStatus.CREATED, "Orden registrada correctamente"));

        mockMvc.perform(post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vehicle_id\":\"V-001\",\"customer_id\":\"customer-007\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchase_order_id").value("po-test-001"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("Orden registrada correctamente"));

        ArgumentCaptor<CreatePurchaseOrderCommand> captor = ArgumentCaptor.forClass(CreatePurchaseOrderCommand.class);
        verify(vehicleDomainService).createPurchaseOrder(captor.capture());
        assertThat(captor.getValue().vehicleId()).isEqualTo("V-001");
        assertThat(captor.getValue().customerId()).isEqualTo("customer-007");
        assertThat(captor.getValue().purchaseOrderId()).isNull();
    }

    @Test
    void shouldRejectPurchaseOrderWithoutVehicleId() throws Exception {
        mockMvc.perform(post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customer_id\":\"customer-007\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flow_code").value("ERR_VALIDATION_01"))
                .andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_ERROR.message()))
                .andExpect(jsonPath("$.details[0].field").value("vehicle_id"));

        verifyNoInteractions(vehicleDomainService);
    }

    @Test
    void shouldRejectPurchaseOrderWithInvalidIdentifierFormat() throws Exception {
        mockMvc.perform(post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vehicle_id\":\"V-001\",\"customer_id\":\"customer-007\","
                                + "\"purchase_order_id\":\"po 001!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flow_code").value("ERR_VALIDATION_01"))
                .andExpect(jsonPath("$.details[0].field").value("purchase_order_id"));

        verifyNoInteractions(vehicleDomainService);
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flow_code").value("ERR_MALFORMED_BODY_01"))
                .andExpect(jsonPath("$.message").value(ErrorCode.MALFORMED_BODY.message()));
    }

    @Test
    void shouldRegisterPayment() throws Exception {
        when(vehicleDomainService.registerPayment(any())).thenReturn(new PaymentRegistered(
                "po-1001", PaymentStatus.PAYMENT_CONFIGURED, "CASH"));

        mockMvc.perform(post("/payment-methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"purchase_order_id\":\"po-1001\",\"payment_type\":\"CASH\","
                                + "\"financial_institution\":\"Santander\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchase_order_id").value("po-1001"))
                .andExpect(jsonPath("$.status").value("PAYMENT_CONFIGURED"))
                .andExpect(jsonPath("$.payment_type").value("CASH"));
    }

    @Test
    void shouldRejectPaymentWithoutRequiredFields() throws Exception {
        mockMvc.perform(post("/payment-methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"purchase_order_id\":\"po-1001\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flow_code").value("ERR_VALIDATION_01"))
                .andExpect(jsonPath("$.details[*].field", hasItem("payment_type")))
                .andExpect(jsonPath("$.details[*].field", hasItem("financial_institution")));

        verifyNoInteractions(vehicleDomainService);
    }

    @Test
    void shouldHideUnexpectedErrorDetails() throws Exception {
        when(vehicleDomainService.getVehicleById("boom"))
                .thenThrow(new IllegalStateException("database password leaked"));

        mockMvc.perform(get("/vehicles/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.flow_code").value("ERR_INTERNAL_01"))
                .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_ERROR.message()))
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void shouldRejectUnsupportedMethod() throws Exception {
        mockMvc.perform(put("/vehicles"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.flow_code").value("ERR_METHOD_NOT_ALLOWED_01"));
    }

    @Test
    void shouldReturnNotFoundForUnknownEndpoint() throws Exception {
        mockMvc.perform(get("/unknown-endpoint"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flow_code").value("ERR_ENDPOINT_NOT_FOUND_01"));
    }

    private static VehicleInfo vehicleInfo() {
        return new VehicleInfo("V-001", "M-001", "Toyota", "Corolla", 2024, true,
                new BigDecimal("28900.00"), "MXN");
    }
}
