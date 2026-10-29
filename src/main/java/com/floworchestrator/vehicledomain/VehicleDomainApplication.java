package com.floworchestrator.vehicledomain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio vehicle-domain-service.
 * Arranca el contexto Spring Boot que expone el dominio de vehículos, órdenes de compra y pagos.
 */
@SpringBootApplication
public class VehicleDomainApplication {

    /**
     * Inicia la aplicación Spring Boot.
     */
    public static void main(String[] args) {
        SpringApplication.run(VehicleDomainApplication.class, args);
    }
}
