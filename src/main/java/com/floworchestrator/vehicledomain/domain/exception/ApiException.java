package com.floworchestrator.vehicledomain.domain.exception;

/**
 * Excepción de dominio que asocia un {@link ErrorCode} a un error de negocio.
 * El estado HTTP, el flow_code y el mensaje se resuelven a partir de ese código.
 */
public class ApiException extends RuntimeException {

    private final transient ErrorCode errorCode;

    /**
     * Crea la excepción usando el mensaje por defecto del código de error.
     */
    public ApiException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    /**
     * Crea la excepción con un mensaje específico que reemplaza al del código de error.
     */
    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Crea la excepción con un mensaje específico y la causa original del fallo.
     */
    public ApiException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
