package com.itinerarios.airport.adapter;

import com.itinerarios.airport.dto.adapter.ColombiaAirportResponse;
import com.itinerarios.airport.exception.ExternalProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Único punto de integración con API Colombia (https://api-colombia.com/api/v1).
 * Flujo obligatorio (sección 21): Controller -> Service -> Adapter -> API externa.
 * El Controller NUNCA debe llamar directamente a este componente.
 *
 * Nota de resiliencia: en Nivel 1 solo se maneja timeout + traducción de errores.
 * Retry, backoff, jitter y Circuit Breaker se agregan en Nivel 2 (sección 28-29).
 */
@Component
public class ColombiaAirportAdapter {

    private static final Logger log = LoggerFactory.getLogger(ColombiaAirportAdapter.class);

    private final RestClient colombiaApiRestClient;

    public ColombiaAirportAdapter(RestClient colombiaApiRestClient) {
        this.colombiaApiRestClient = colombiaApiRestClient;
    }

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
}
