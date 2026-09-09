package com.itinerarios.airport.exception;

import com.itinerarios.airport.config.CorrelationIdFilter;
import com.itinerarios.airport.exception.AirportNotFoundException;
import com.itinerarios.airport.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    private AutoCloseable mocks;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        handler = new GlobalExceptionHandler();

        MDC.clear();

        when(request.getRequestURI())
                .thenReturn("/api/v1/airports/1");
    }

    @AfterEach
    void tearDown() throws Exception {
        MDC.clear();
        mocks.close();
    }

    @Test
    void shouldHandleAirportNotFoundException() {

        String correlationId = "test-correlation-id";

        MDC.put(
                CorrelationIdFilter.MDC_KEY,
                correlationId
        );

        AirportNotFoundException exception =
                new AirportNotFoundException(
                        "Airport with id 1 not found"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(exception, request);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody())
                .isNotNull();

        ErrorResponse body = response.getBody();

        assertThat(body.status())
                .isEqualTo(HttpStatus.NOT_FOUND.value());

        assertThat(body.error())
                .isEqualTo("NOT_FOUND");

        assertThat(body.message())
                .isEqualTo("Airport with id 1 not found");

        assertThat(body.path())
                .isEqualTo("/api/v1/airports/1");

        assertThat(body.correlationId())
                .isEqualTo(correlationId);

        assertThat(body.timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleValidationException() {

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError nameError = new FieldError(
                "airportRequest",
                "name",
                "must not be blank"
        );

        FieldError cityError = new FieldError(
                "airportRequest",
                "city",
                "must not be blank"
        );

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(nameError, cityError));

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        MDC.put(
                CorrelationIdFilter.MDC_KEY,
                "validation-correlation-id"
        );

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(exception, request);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        ErrorResponse body = response.getBody();

        assertThat(body.status())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        assertThat(body.error())
                .isEqualTo("VALIDATION_ERROR");

        assertThat(body.message())
                .isEqualTo(
                        "name: must not be blank; " +
                                "city: must not be blank"
                );

        assertThat(body.path())
                .isEqualTo("/api/v1/airports/1");

        assertThat(body.correlationId())
                .isEqualTo("validation-correlation-id");

        assertThat(body.timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleValidationExceptionWithSingleFieldError() {

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError fieldError = new FieldError(
                "airportRequest",
                "iataCode",
                "must not be null"
        );

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(exception, request);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().message())
                .isEqualTo("iataCode: must not be null");

        assertThat(response.getBody().error())
                .isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldHandleExternalProviderException() {

        String correlationId = "external-provider-correlation-id";

        MDC.put(
                CorrelationIdFilter.MDC_KEY,
                correlationId
        );

        ExternalProviderException exception =
                new ExternalProviderException(
                        "API Colombia is unavailable"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleExternalProvider(
                        exception,
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);

        assertThat(response.getBody())
                .isNotNull();

        ErrorResponse body = response.getBody();

        assertThat(body.status())
                .isEqualTo(HttpStatus.BAD_GATEWAY.value());

        assertThat(body.error())
                .isEqualTo("EXTERNAL_PROVIDER_ERROR");

        assertThat(body.message())
                .isEqualTo("API Colombia is unavailable");

        assertThat(body.path())
                .isEqualTo("/api/v1/airports/1");

        assertThat(body.correlationId())
                .isEqualTo(correlationId);

        assertThat(body.timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleGenericException() {

        MDC.put(
                CorrelationIdFilter.MDC_KEY,
                "generic-error-correlation-id"
        );

        Exception exception =
                new RuntimeException(
                        "Database connection failed"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneric(
                        exception,
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(response.getBody())
                .isNotNull();

        ErrorResponse body = response.getBody();

        assertThat(body.status())
                .isEqualTo(
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                );

        assertThat(body.error())
                .isEqualTo("INTERNAL_SERVER_ERROR");

        /*
         * El mensaje real de la excepción NO se expone.
         * El handler utiliza deliberadamente "Unexpected error".
         */
        assertThat(body.message())
                .isEqualTo("Unexpected error");

        assertThat(body.path())
                .isEqualTo("/api/v1/airports/1");

        assertThat(body.correlationId())
                .isEqualTo("generic-error-correlation-id");

        assertThat(body.timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleExceptionWithoutCorrelationId() {

        MDC.remove(CorrelationIdFilter.MDC_KEY);

        AirportNotFoundException exception =
                new AirportNotFoundException(
                        "Airport not found"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        exception,
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().correlationId())
                .isNull();

        assertThat(response.getBody().status())
                .isEqualTo(404);

        assertThat(response.getBody().error())
                .isEqualTo("NOT_FOUND");

        assertThat(response.getBody().message())
                .isEqualTo("Airport not found");
    }

    @Test
    void shouldUseCurrentRequestUriInErrorResponse() {

        when(request.getRequestURI())
                .thenReturn("/api/v1/airports/search");

        AirportNotFoundException exception =
                new AirportNotFoundException(
                        "No airport found"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        exception,
                        request
                );

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().path())
                .isEqualTo("/api/v1/airports/search");
    }
}