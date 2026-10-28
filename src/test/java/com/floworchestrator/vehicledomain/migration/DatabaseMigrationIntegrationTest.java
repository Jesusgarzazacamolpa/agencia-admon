package com.floworchestrator.vehicledomain.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers(disabledWithoutDocker = true)
class DatabaseMigrationIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldApplyAllMigrationsSuccessfully() {
        List<String> versions = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank",
                String.class);

        assertThat(versions).containsExactly("1", "2");
    }

    @Test
    void shouldCreateModelYearColumn() {
        Integer columns = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'vehicle' AND column_name = 'model_year'
                """, Integer.class);

        assertThat(columns).isEqualTo(1);
    }

    @Test
    void shouldCreateBusinessUniqueIndexes() {
        List<String> indexes = jdbcTemplate.queryForList("""
                SELECT indexname FROM pg_indexes
                WHERE schemaname = 'public'
                  AND indexname IN ('uk_vehicle_vehicle_id', 'uk_purchase_order_purchase_order_id')
                ORDER BY indexname
                """, String.class);

        assertThat(indexes).containsExactly("uk_purchase_order_purchase_order_id", "uk_vehicle_vehicle_id");
    }

    @Test
    void shouldCreateFilterIndexes() {
        List<String> indexes = jdbcTemplate.queryForList("""
                SELECT indexname FROM pg_indexes
                WHERE schemaname = 'public'
                  AND indexname IN ('idx_vehicle_brand', 'idx_vehicle_model',
                                    'idx_purchase_order_customer', 'idx_purchase_order_vehicle',
                                    'idx_payment_method_purchase_order')
                ORDER BY indexname
                """, String.class);

        assertThat(indexes).containsExactly(
                "idx_payment_method_purchase_order",
                "idx_purchase_order_customer",
                "idx_purchase_order_vehicle",
                "idx_vehicle_brand",
                "idx_vehicle_model");
    }
}
