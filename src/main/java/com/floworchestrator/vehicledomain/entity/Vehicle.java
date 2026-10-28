package com.floworchestrator.vehicledomain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vehicle",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehicle_vehicle_id", columnNames = "vehicle_id"),
        indexes = {
                @Index(name = "idx_vehicle_brand", columnList = "brand"),
                @Index(name = "idx_vehicle_model", columnList = "model")
        })
@Getter
@Setter
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "vehicle_id", nullable = false, length = 100)
    private String vehicleId;

    @Column(name = "model_id", nullable = false, length = 100)
    private String modelId;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "model_year")
    private Integer year;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private boolean available;
}
