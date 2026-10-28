package com.floworchestrator.vehicledomain.repository;

import com.floworchestrator.vehicledomain.domain.model.PurchaseOrderStatus;
import com.floworchestrator.vehicledomain.entity.PurchaseOrder;
import com.floworchestrator.vehicledomain.entity.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class VehicleRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @BeforeEach
    void setUp() {
        purchaseOrderRepository.deleteAll();
        vehicleRepository.deleteAll();
        saveVehicle("model-101", "Toyota", "Corolla", 2024, "28900.00", true);
        saveVehicle("model-102", "Toyota", "RAV4", 2024, "500000.00", true);
        saveVehicle("model-103", "Honda", "Civic", 2023, "420000.00", false);
        saveVehicle("model-104", "Mazda", "Mazda 3", 2025, "455000.00", false);
    }

    @Test
    void shouldFilterByBrandPartiallyAndIgnoringCase() {
        Page<Vehicle> result = vehicleRepository.findWithFilters("toyo", null, null, null, null, null,
                PageRequest.of(0, 10, Sort.by("model")));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Vehicle::getModel).containsExactly("Corolla", "RAV4");
    }

    @Test
    void shouldFilterByModelPartially() {
        Page<Vehicle> result = vehicleRepository.findWithFilters(null, "civ", null, null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Vehicle::getVehicleId).containsExactly("model-103");
    }

    @Test
    void shouldFilterByAvailability() {
        Page<Vehicle> result = vehicleRepository.findWithFilters(null, null, true, null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(Vehicle::isAvailable);
    }

    @Test
    void shouldFilterByPriceRange() {
        Page<Vehicle> result = vehicleRepository.findWithFilters(null, null, null,
                new BigDecimal("300000.00"), new BigDecimal("460000.00"), null, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Vehicle::getVehicleId)
                .containsExactlyInAnyOrder("model-103", "model-104");
    }

    @Test
    void shouldFilterByYear() {
        Page<Vehicle> result = vehicleRepository.findWithFilters(null, null, null, null, null, 2024,
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(vehicle -> vehicle.getYear() == 2024);
    }

    @Test
    void shouldCombineAllFilters() {
        Page<Vehicle> result = vehicleRepository.findWithFilters("Toyota", null, true,
                new BigDecimal("400000.00"), null, 2024, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Vehicle::getVehicleId).containsExactly("model-102");
    }

    @Test
    void shouldPaginateResults() {
        Page<Vehicle> firstPage = vehicleRepository.findWithFilters(null, null, null, null, null, null,
                PageRequest.of(0, 2, Sort.by("brand", "model")));

        assertThat(firstPage.getTotalElements()).isEqualTo(4);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent()).hasSize(2);
    }

    @Test
    void shouldFindAvailableVehicles() {
        assertThat(vehicleRepository.findByAvailableTrue()).hasSize(2);
    }

    @Test
    void shouldFindVehicleByBusinessId() {
        assertThat(vehicleRepository.findByVehicleId("model-101")).isPresent();
        assertThat(vehicleRepository.findByVehicleId("model-999")).isEmpty();
    }

    @Test
    void shouldAssignCreatedAtAndDetectExistingPurchaseOrder() {
        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseOrderId("po-it-001");
        order.setCustomerId("customer-001");
        order.setVehicleId("model-101");
        order.setStatus(PurchaseOrderStatus.CREATED);

        purchaseOrderRepository.saveAndFlush(order);

        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(purchaseOrderRepository.existsByPurchaseOrderId("po-it-001")).isTrue();
        assertThat(purchaseOrderRepository.existsByPurchaseOrderId("po-it-999")).isFalse();
    }

    @Test
    void shouldRejectDuplicateVehicleBusinessId() {
        Vehicle duplicate = new Vehicle();
        duplicate.setVehicleId("model-101");
        duplicate.setModelId("model-101");
        duplicate.setBrand("Toyota");
        duplicate.setModel("Corolla");
        duplicate.setPrice(new BigDecimal("28900.00"));
        duplicate.setCurrency("MXN");
        duplicate.setAvailable(true);

        assertThatThrownBy(() -> vehicleRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void saveVehicle(String vehicleId, String brand, String model, int year, String price, boolean available) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(vehicleId);
        vehicle.setModelId(vehicleId);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setYear(year);
        vehicle.setPrice(new BigDecimal(price));
        vehicle.setCurrency("MXN");
        vehicle.setAvailable(available);
        vehicleRepository.save(vehicle);
    }
}
