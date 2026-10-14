package com.floworchestrator.vehicledomain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vehicle")
@Getter
@Setter
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String vehicleId;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String modelId;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String brand;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String model;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, columnDefinition = "VARCHAR(3)")
    private String currency;

    @Column(nullable = false)
    private boolean available;
}
