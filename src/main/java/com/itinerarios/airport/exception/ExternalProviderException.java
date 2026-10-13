package com.itinerarios.airport.exception;

/**
 * Se lanza cuando la integración con API Colombia falla
 * (timeout, error 5xx, respuesta inválida, etc).
 * Se traduce a HTTP 502/503/504 en el GlobalExceptionHandler.
 */
public class ExternalProviderException extends RuntimeException {

    public ExternalProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalProviderException(String message) {
        super(message);
    }
}
