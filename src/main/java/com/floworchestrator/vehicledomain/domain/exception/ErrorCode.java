package com.floworchestrator.vehicledomain.domain.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VEHICLE_NOT_FOUND("ERR_VEHICLE_NOT_FOUND_01", HttpStatus.NOT_FOUND,
            "No se encontró el vehículo solicitado. Verifica el identificador e inténtalo de nuevo."),
    PURCHASE_ORDER_NOT_FOUND("ERR_PURCHASE_ORDER_NOT_FOUND_01", HttpStatus.NOT_FOUND,
            "No se encontró la orden de compra indicada. Verifica el identificador e inténtalo de nuevo."),
    PURCHASE_ORDER_ALREADY_EXISTS("ERR_PURCHASE_ORDER_EXISTS_01", HttpStatus.CONFLICT,
            "Ya existe una orden de compra con ese identificador. Usa uno diferente o consulta la orden existente."),
    INVALID_PRICE_RANGE("ERR_INVALID_PRICE_RANGE_01", HttpStatus.BAD_REQUEST,
            "El precio mínimo no puede ser mayor que el precio máximo. Ajusta el rango de búsqueda."),
    INVALID_PARAMETER("ERR_INVALID_PARAM_01", HttpStatus.BAD_REQUEST,
            "Uno o más parámetros de la solicitud no son válidos. Revisa los valores enviados."),
    VALIDATION_ERROR("ERR_VALIDATION_01", HttpStatus.BAD_REQUEST,
            "La información enviada no es válida. Corrige los campos indicados."),
    MALFORMED_BODY("ERR_MALFORMED_BODY_01", HttpStatus.BAD_REQUEST,
            "El cuerpo de la solicitud no es un JSON válido. Revisa el formato enviado."),
    DATA_CONFLICT("ERR_DATA_CONFLICT_01", HttpStatus.CONFLICT,
            "Ya existe un registro con la información proporcionada."),
    ENDPOINT_NOT_FOUND("ERR_ENDPOINT_NOT_FOUND_01", HttpStatus.NOT_FOUND,
            "El recurso solicitado no existe. Verifica la ruta de la API."),
    METHOD_NOT_ALLOWED("ERR_METHOD_NOT_ALLOWED_01", HttpStatus.METHOD_NOT_ALLOWED,
            "El método HTTP no está permitido para este recurso."),
    INTERNAL_ERROR("ERR_INTERNAL_01", HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocurrió un error inesperado. Inténtalo de nuevo o contacta al equipo de soporte.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
