package com.floworchestrator.vehicledomain.repository;

import com.floworchestrator.vehicledomain.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByVehicleId(String vehicleId);

    List<Vehicle> findByAvailableTrue();

    @Query("""
            SELECT v FROM Vehicle v
            WHERE (CAST(:brand AS string) IS NULL
                   OR LOWER(v.brand) LIKE LOWER(CONCAT('%', CAST(:brand AS string), '%')))
              AND (CAST(:model AS string) IS NULL
                   OR LOWER(v.model) LIKE LOWER(CONCAT('%', CAST(:model AS string), '%')))
              AND (CAST(:available AS boolean) IS NULL OR v.available = :available)
              AND (CAST(:minPrice AS big_decimal) IS NULL OR v.price >= :minPrice)
              AND (CAST(:maxPrice AS big_decimal) IS NULL OR v.price <= :maxPrice)
              AND (CAST(:year AS integer) IS NULL OR v.year = :year)
            """)
    Page<Vehicle> findWithFilters(@Param("brand") String brand,
                                  @Param("model") String model,
                                  @Param("available") Boolean available,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  @Param("year") Integer year,
                                  Pageable pageable);
}
