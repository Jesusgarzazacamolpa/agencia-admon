package com.floworchestrator.vehicledomain.web.controller;

import com.floworchestrator.vehicledomain.service.VehicleDomainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleDomainController.class)
class VehicleDomainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleDomainService vehicleDomainService;

    @Test
    void shouldReturnOkForHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnVehicleById() throws Exception {
        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("vehicle_id", "V-001");
        vehicleMap.put("model_id", "M-001");
        vehicleMap.put("brand", "Toyota");
        vehicleMap.put("model", "Corolla");
        vehicleMap.put("price", new BigDecimal("28900.00"));
        vehicleMap.put("currency", "MXN");
        vehicleMap.put("available", true);

        when(vehicleDomainService.getVehicleById("V-001")).thenReturn(vehicleMap);

        mockMvc.perform(get("/vehicles/V-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicle_id").value("V-001"))
                .andExpect(jsonPath("$.brand").value("Toyota"));
    }

    @Test
    void shouldReturnNotFoundWhenVehicleDoesNotExist() throws Exception {
        when(vehicleDomainService.getVehicleById("unknown"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found: unknown"));

        mockMvc.perform(get("/vehicles/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreatePurchaseOrder() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("purchase_order_id", "po-test-001");
        response.put("status", "CREATED");

        when(vehicleDomainService.createPurchaseOrder(any(Map.class))).thenReturn(response);

        mockMvc.perform(post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vehicle_id\":\"V-001\",\"customer_id\":\"customer-007\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchase_order_id").value("po-test-001"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldReturnBadRequestWhenVehicleIdMissing() throws Exception {
        when(vehicleDomainService.createPurchaseOrder(any(Map.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required field is missing: vehicle_id"));

        mockMvc.perform(post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customer_id\":\"customer-007\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRegisterPayment() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("purchase_order_id", "po-1001");
        response.put("status", "PAYMENT_CONFIGURED");
        response.put("payment_type", "CASH");

        when(vehicleDomainService.registerPayment(any(Map.class))).thenReturn(response);

        mockMvc.perform(post("/payment-methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"purchase_order_id\":\"po-1001\",\"payment_type\":\"CASH\",\"financial_institution\":\"Santander\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAYMENT_CONFIGURED"))
                .andExpect(jsonPath("$.payment_type").value("CASH"));
    }

    @Test
    void shouldReturnBadRequestWhenPaymentFieldMissing() throws Exception {
        when(vehicleDomainService.registerPayment(any(Map.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required field is missing: payment_type"));

        mockMvc.perform(post("/payment-methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"purchase_order_id\":\"po-1001\"}"))
                .andExpect(status().isBadRequest());
    }
}
