package com.itinerarios.airport.adapter;

import com.itinerarios.airport.dto.adapter.ColombiaAirportResponse;
import com.itinerarios.airport.exception.ExternalProviderException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Unico punto de integracion con API Colombia (https://api-colombia.com/api/v1).
 * Flujo obligatorio (seccion 21): Controller -> Service -> Adapter -> API externa.
 * El Controller NUNCA debe llamar directamente a este componente.
 *
 * Resiliencia (seccion 28-29, Fase 10): cada llamada HTTP real esta protegida
 * por Retry (con backoff exponencial + jitter, configurado en application.yml)
 * y Circuit Breaker. Ambas son operaciones GET, por lo tanto idempotentes -
 * reintentarlas es seguro.
 *
 * El orden de los aspectos importa: Resilience4j aplica Retry como el
 * aspecto MAS externo por defecto (mayor prioridad), de modo que cada
 * intento individual dentro de un Retry pasa por el Circuit Breaker; si el
 * circuito esta abierto, el Retry deja de tener sentido y el fallback se
 * dispara inmediatamente en el primer intento.
 */
@Component
public class ColombiaAirportAdapter {

    private static final Logger log = LoggerFactory.getLogger(ColombiaAirportAdapter.class);
    private static final String RESILIENCE_INSTANCE = "colombiaApi";

    private final RestClient colombiaApiRestClient;

    public ColombiaAirportAdapter(RestClient colombiaApiRestClient) {
        this.colombiaApiRestClient = colombiaApiRestClient;
    }

    @Retry(name = RESILIENCE_INSTANCE)
    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "findAllFallback")
    public List<ColombiaAirportResponse> findAll() {
        try {
            ColombiaAirportResponse[] response = colombiaApiRestClient.get()
                    .uri("/Airport")
                    .retrieve()
                    .body(ColombiaAirportResponse[].class);
            return response != null ? List.of(response) : List.of();
        } catch (RestClientException ex) {
            log.error("Error calling API Colombia GET /Airport", ex);
            throw new ExternalProviderException("Failed to fetch airports from API Colombia", ex);
        }
    }

    public ColombiaAirportResponse findByIataCode(String iataCode) {
        List<ColombiaAirportResponse> matches = findByKeyword(iataCode);
        return matches.stream()
                .filter(a -> iataCode.equalsIgnoreCase(a.iataCode()))
                .findFirst()
                .orElse(null);
    }

    @Retry(name = RESILIENCE_INSTANCE)
    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "findByKeywordFallback")
    public List<ColombiaAirportResponse> findByKeyword(String keyword) {
        try {
            ColombiaAirportResponse[] response = colombiaApiRestClient.get()
                    .uri("/Airport/search/{keyword}", keyword)
                    .retrieve()
                    .body(ColombiaAirportResponse[].class);
            return response != null ? List.of(response) : List.of();
        } catch (RestClientException ex) {
            log.error("Error calling API Colombia GET /Airport/search/{}", keyword, ex);
            throw new ExternalProviderException("Failed to search airports from API Colombia", ex);
        }
    }

    // ---- Fallbacks: firma = misma firma del metodo + Throwable al final ----

    @SuppressWarnings("unused")
    private List<ColombiaAirportResponse> findAllFallback(Throwable throwable) {
        log.error("Circuit breaker '{}' activo: no se pudo listar aeropuertos de API Colombia", RESILIENCE_INSTANCE, throwable);
        throw new ExternalProviderException("API Colombia no disponible (circuit breaker abierto)", throwable);
    }

    @SuppressWarnings("unused")
    private List<ColombiaAirportResponse> findByKeywordFallback(String keyword, Throwable throwable) {
        log.error("Circuit breaker '{}' activo: no se pudo buscar '{}' en API Colombia", RESILIENCE_INSTANCE, keyword, throwable);
        throw new ExternalProviderException("API Colombia no disponible (circuit breaker abierto)", throwable);
    }
}
