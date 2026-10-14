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
    
    Page<Vehicle> findAll(Pageable pageable);
    
    List<Vehicle> findByBrand(String brand);
    
    List<Vehicle> findByAvailableTrue();
    
    @Query(value = """
        SELECT v.id, v.vehicle_id, v.model_id, v.brand, v.model, v.price, v.currency, v.available
        FROM vehicle v
        WHERE (:brand IS NULL OR LOWER(v.brand::text) = LOWER(:brand))
          AND (:model IS NULL OR LOWER(v.model::text) = LOWER(:model))
          AND (:available IS NULL OR v.available = :available)
          AND (:minPrice IS NULL OR v.price >= :minPrice)
          AND (:maxPrice IS NULL OR v.price <= :maxPrice)
        ORDER BY v.id
        """, countQuery = """
        SELECT COUNT(*) FROM vehicle v
        WHERE (:brand IS NULL OR LOWER(v.brand::text) = LOWER(:brand))
          AND (:model IS NULL OR LOWER(v.model::text) = LOWER(:model))
          AND (:available IS NULL OR v.available = :available)
          AND (:minPrice IS NULL OR v.price >= :minPrice)
          AND (:maxPrice IS NULL OR v.price <= :maxPrice)
        """, nativeQuery = true)
    Page<Vehicle> findWithFilters(@Param("brand") String brand,
                                   @Param("model") String model,
                                   @Param("available") Boolean available,
                                   @Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);
}
