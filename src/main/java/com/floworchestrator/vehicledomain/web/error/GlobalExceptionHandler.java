package com.floworchestrator.vehicledomain.web.error;

import com.floworchestrator.vehicledomain.domain.exception.ApiException;
import com.floworchestrator.vehicledomain.domain.exception.ErrorCode;
import com.floworchestrator.vehicledomain.web.dto.ApiErrorResponse;
import com.floworchestrator.vehicledomain.web.dto.FieldValidationError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Manejador global de excepciones de la API. Traduce cada fallo al contrato {@link ApiErrorResponse}
 * con su estado HTTP, flow_code, ruta, trace_id y detalles de validación.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TRACE_ID_MDC_KEY = "traceId";
    private static final String DEFAULT_PARAMETER_NAME = "parámetro";
    private static final Pattern CAMEL_CASE_BOUNDARY = Pattern.compile("([a-z0-9])([A-Z])");

    /**
     * Responde con el estado y flow_code del ErrorCode; registra los 5xx como error y el resto como warning.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        if (errorCode.status().is5xxServerError()) {
            log.error("flow_code={} status={} path={} message={}",
                    errorCode.code(), errorCode.status().value(), request.getRequestURI(),
                    exception.getMessage(), exception);
        } else {
            log.warn("flow_code={} status={} path={} message={}",
                    errorCode.code(), errorCode.status().value(), request.getRequestURI(),
                    exception.getMessage());
        }
        return buildResponse(errorCode, exception.getMessage(), request, List.of());
    }

    /**
     * Devuelve 400 VALIDATION_ERROR con un detalle por cada campo inválido del cuerpo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleBodyValidation(MethodArgumentNotValidException exception,
                                                                 HttpServletRequest request) {
        List<FieldValidationError> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldValidationError(toApiFieldName(error.getField()), error.getDefaultMessage()))
                .toList();
        log.warn("flow_code={} status={} path={} validation_errors={}",
                ErrorCode.VALIDATION_ERROR.code(), ErrorCode.VALIDATION_ERROR.status().value(),
                request.getRequestURI(), details);
        return buildResponse(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.message(), request, details);
    }

    /**
     * Devuelve 400 INVALID_PARAMETER cuando falla la validación de parámetros de un handler.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleParameterValidation(HandlerMethodValidationException exception,
                                                                      HttpServletRequest request) {
        List<FieldValidationError> details = exception.getAllValidationResults().stream()
                .flatMap(result -> toFieldErrors(result).stream())
                .toList();
        log.warn("flow_code={} status={} path={} validation_errors={}",
                ErrorCode.INVALID_PARAMETER.code(), ErrorCode.INVALID_PARAMETER.status().value(),
                request.getRequestURI(), details);
        return buildResponse(ErrorCode.INVALID_PARAMETER, ErrorCode.INVALID_PARAMETER.message(), request, details);
    }

    /**
     * Devuelve 400 INVALID_PARAMETER cuando falla una restricción de bean validation.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception,
                                                                      HttpServletRequest request) {
        List<FieldValidationError> details = exception.getConstraintViolations().stream()
                .map(this::toFieldError)
                .toList();
        log.warn("flow_code={} status={} path={} validation_errors={}",
                ErrorCode.INVALID_PARAMETER.code(), ErrorCode.INVALID_PARAMETER.status().value(),
                request.getRequestURI(), details);
        return buildResponse(ErrorCode.INVALID_PARAMETER, ErrorCode.INVALID_PARAMETER.message(), request, details);
    }

    /**
     * Devuelve 400 INVALID_PARAMETER cuando un parámetro no se puede convertir al tipo esperado.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                               HttpServletRequest request) {
        String message = "El parámetro '" + exception.getName()
                + "' tiene un formato incorrecto. Verifica el valor enviado.";
        FieldValidationError detail = new FieldValidationError(exception.getName(), message);
        log.warn("flow_code={} status={} path={} parameter={}",
                ErrorCode.INVALID_PARAMETER.code(), ErrorCode.INVALID_PARAMETER.status().value(),
                request.getRequestURI(), exception.getName());
        return buildResponse(ErrorCode.INVALID_PARAMETER, message, request, List.of(detail));
    }

    /**
     * Devuelve 400 MALFORMED_BODY cuando el cuerpo de la petición no es JSON válido.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception,
                                                                 HttpServletRequest request) {
        log.warn("flow_code={} status={} path={}",
                ErrorCode.MALFORMED_BODY.code(), ErrorCode.MALFORMED_BODY.status().value(),
                request.getRequestURI());
        return buildResponse(ErrorCode.MALFORMED_BODY, ErrorCode.MALFORMED_BODY.message(), request, List.of());
    }

    /**
     * Devuelve 404 ENDPOINT_NOT_FOUND cuando la ruta solicitada no existe.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEndpointNotFound(NoResourceFoundException exception,
                                                                   HttpServletRequest request) {
        log.warn("flow_code={} status={} path={}",
                ErrorCode.ENDPOINT_NOT_FOUND.code(), ErrorCode.ENDPOINT_NOT_FOUND.status().value(),
                request.getRequestURI());
        return buildResponse(ErrorCode.ENDPOINT_NOT_FOUND, ErrorCode.ENDPOINT_NOT_FOUND.message(), request, List.of());
    }

    /**
     * Devuelve 405 METHOD_NOT_ALLOWED cuando el verbo HTTP no está soportado en la ruta.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception,
                                                                   HttpServletRequest request) {
        log.warn("flow_code={} status={} path={} method={}",
                ErrorCode.METHOD_NOT_ALLOWED.code(), ErrorCode.METHOD_NOT_ALLOWED.status().value(),
                request.getRequestURI(), exception.getMethod());
        return buildResponse(ErrorCode.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED.message(), request, List.of());
    }

    /**
     * Devuelve 409 DATA_CONFLICT ante violaciones de integridad de la base de datos.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception,
                                                                HttpServletRequest request) {
        log.error("flow_code={} status={} path={}",
                ErrorCode.DATA_CONFLICT.code(), ErrorCode.DATA_CONFLICT.status().value(),
                request.getRequestURI(), exception);
        return buildResponse(ErrorCode.DATA_CONFLICT, ErrorCode.DATA_CONFLICT.message(), request, List.of());
    }

    /**
     * Devuelve 500 INTERNAL_ERROR como red de seguridad para cualquier excepción no controlada.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("flow_code={} status={} path={}",
                ErrorCode.INTERNAL_ERROR.code(), ErrorCode.INTERNAL_ERROR.status().value(),
                request.getRequestURI(), exception);
        return buildResponse(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.message(), request, List.of());
    }

    private List<FieldValidationError> toFieldErrors(ParameterValidationResult result) {
        String parameterName = Optional.ofNullable(result.getMethodParameter().getParameterName())
                .map(GlobalExceptionHandler::leafName)
                .orElse(DEFAULT_PARAMETER_NAME);
        return result.getResolvableErrors().stream()
                .map(error -> new FieldValidationError(parameterName, error.getDefaultMessage()))
                .toList();
    }

    private FieldValidationError toFieldError(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath() == null
                ? DEFAULT_PARAMETER_NAME
                : leafName(violation.getPropertyPath().toString());
        return new FieldValidationError(field, violation.getMessage());
    }

    /**
     * Convierte el nombre camelCase del campo al formato snake_case usado por la API.
     */
    private static String toApiFieldName(String field) {
        if (field == null || field.isBlank()) {
            return DEFAULT_PARAMETER_NAME;
        }
        return CAMEL_CASE_BOUNDARY.matcher(field).replaceAll("$1_$2").toLowerCase(Locale.ROOT);
    }

    /**
     * Extrae el último segmento de una ruta de propiedad (por ejemplo, "page" de "getVehicles.page").
     */
    private static String leafName(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return DEFAULT_PARAMETER_NAME;
        }
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(ErrorCode errorCode, String message,
                                                           HttpServletRequest request,
                                                           List<FieldValidationError> details) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                errorCode.status().value(),
                errorCode.status().getReasonPhrase(),
                message,
                errorCode.code(),
                request.getRequestURI(),
                MDC.get(TRACE_ID_MDC_KEY),
                details);
        return ResponseEntity.status(errorCode.status()).body(body);
    }
}
