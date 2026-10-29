package com.floworchestrator.vehicledomain.web.dto;

/**
 * Detalle de un error de validación asociado a un campo o parámetro concreto.
 */
public record FieldValidationError(String field, String message) {
}
