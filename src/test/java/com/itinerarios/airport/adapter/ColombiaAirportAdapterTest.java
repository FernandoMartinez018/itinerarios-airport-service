package com.itinerarios.airport.adapter;

import com.itinerarios.airport.dto.adapter.ColombiaAirportResponse;
import com.itinerarios.airport.exception.ExternalProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ColombiaAirportAdapterTest {

    private RestClient colombiaApiRestClient;

    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    private RestClient.ResponseSpec responseSpec;

    private ColombiaAirportAdapter adapter;

    @BeforeEach
    void setUp() {
        colombiaApiRestClient = mock(RestClient.class);

        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec uriSpec =
                mock(RestClient.RequestHeadersUriSpec.class);

        requestHeadersUriSpec = uriSpec;

        responseSpec = mock(RestClient.ResponseSpec.class);

        adapter = new ColombiaAirportAdapter(colombiaApiRestClient);
    }

    @Test
    void shouldReturnAllAirportsWhenApiReturnsData() {
        ColombiaAirportResponse airport1 =
                createAirport(1L, "El Dorado", "BOG", "SKBO");

        ColombiaAirportResponse airport2 =
                createAirport(2L, "José María Córdova", "MDE", "SKRG");

        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri("/Airport"))
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(new ColombiaAirportResponse[]{
                        airport1,
                        airport2
                });

        List<ColombiaAirportResponse> result =
                adapter.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactly(airport1, airport2);

        verify(colombiaApiRestClient).get();
        verify(requestHeadersUriSpec).uri("/Airport");
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec)
                .body(ColombiaAirportResponse[].class);
    }

    @Test
    void shouldReturnEmptyListWhenFindAllApiReturnsNull() {
        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri("/Airport"))
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(null);

        List<ColombiaAirportResponse> result =
                adapter.findAll();

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenFindAllApiReturnsEmptyArray() {
        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri("/Airport"))
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(new ColombiaAirportResponse[0]);

        List<ColombiaAirportResponse> result =
                adapter.findAll();

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void shouldTranslateRestClientExceptionWhenFindAllFails() {
        RestClientException originalException =
                new RestClientException("Connection timeout");

        when(colombiaApiRestClient.get())
                .thenThrow(originalException);

        assertThatThrownBy(() -> adapter.findAll())
                .isInstanceOf(ExternalProviderException.class)
                .hasMessage("Failed to fetch airports from API Colombia")
                .hasCause(originalException);

        verify(colombiaApiRestClient).get();
    }

    @Test
    void shouldFindAirportByExactIataCode() {
        ColombiaAirportResponse bogota =
                createAirport(1L, "El Dorado", "BOG", "SKBO");

        ColombiaAirportResponse medellin =
                createAirport(2L, "José María Córdova", "MDE", "SKRG");

        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(
                eq("/Airport/search/{keyword}"),
                eq("BOG")
        )).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(new ColombiaAirportResponse[]{
                        bogota,
                        medellin
                });

        ColombiaAirportResponse result =
                adapter.findByIataCode("BOG");

        assertThat(result)
                .isNotNull()
                .isEqualTo(bogota);

        assertThat(result.iataCode())
                .isEqualTo("BOG");

        verify(colombiaApiRestClient).get();
        verify(requestHeadersUriSpec)
                .uri("/Airport/search/{keyword}", "BOG");
    }

    @Test
    void shouldFindAirportByIataCodeIgnoringCase() {
        ColombiaAirportResponse bogota =
                createAirport(1L, "El Dorado", "BOG", "SKBO");

        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(
                eq("/Airport/search/{keyword}"),
                eq("bog")
        )).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(new ColombiaAirportResponse[]{
                        bogota
                });

        ColombiaAirportResponse result =
                adapter.findByIataCode("bog");

        assertThat(result)
                .isNotNull()
                .isEqualTo(bogota);
    }

    @Test
    void shouldReturnNullWhenIataCodeDoesNotMatch() {
        ColombiaAirportResponse medellin =
                createAirport(2L, "José María Córdova", "MDE", "SKRG");

        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(
                eq("/Airport/search/{keyword}"),
                eq("BOG")
        )).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(new ColombiaAirportResponse[]{
                        medellin
                });

        ColombiaAirportResponse result =
                adapter.findByIataCode("BOG");

        assertThat(result)
                .isNull();
    }

    @Test
    void shouldReturnNullWhenFindByIataCodeApiReturnsNull() {
        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(
                eq("/Airport/search/{keyword}"),
                eq("BOG")
        )).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(null);

        ColombiaAirportResponse result =
                adapter.findByIataCode("BOG");

        assertThat(result)
                .isNull();
    }

    @Test
    void shouldReturnNullWhenFindByIataCodeApiReturnsEmptyArray() {
        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(
                eq("/Airport/search/{keyword}"),
                eq("BOG")
        )).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(new ColombiaAirportResponse[0]);

        ColombiaAirportResponse result =
                adapter.findByIataCode("BOG");

        assertThat(result)
                .isNull();
    }

    @Test
    void shouldFindAirportWhenMatchingAirportIsNotFirstResult() {
        ColombiaAirportResponse medellin =
                createAirport(2L, "José María Córdova", "MDE", "SKRG");

        ColombiaAirportResponse bogota =
                createAirport(1L, "El Dorado", "BOG", "SKBO");

        ColombiaAirportResponse cali =
                createAirport(3L, "Alfonso Bonilla Aragón", "CLO", "SKCL");

        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(
                eq("/Airport/search/{keyword}"),
                eq("BOG")
        )).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(new ColombiaAirportResponse[]{
                        medellin,
                        bogota,
                        cali
                });

        ColombiaAirportResponse result =
                adapter.findByIataCode("BOG");

        assertThat(result)
                .isEqualTo(bogota);
    }

    @Test
    void shouldTranslateRestClientExceptionWhenFindByKeywordFails() {
        RestClientException originalException =
                new RestClientException("Connection refused");

        when(colombiaApiRestClient.get())
                .thenThrow(originalException);

        assertThatThrownBy(() -> adapter.findByKeyword("BOG"))
                .isInstanceOf(ExternalProviderException.class)
                .hasMessage("Failed to search airports from API Colombia")
                .hasCause(originalException);

        verify(colombiaApiRestClient).get();
    }

    @Test
    void shouldReturnAirportsWhenFindByKeywordSucceeds() {
        ColombiaAirportResponse airport1 =
                createAirport(1L, "El Dorado", "BOG", "SKBO");

        ColombiaAirportResponse airport2 =
                createAirport(2L, "José María Córdova", "MDE", "SKRG");

        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(
                eq("/Airport/search/{keyword}"),
                eq("Colombia")
        )).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(new ColombiaAirportResponse[]{
                        airport1,
                        airport2
                });

        List<ColombiaAirportResponse> result =
                adapter.findByKeyword("Colombia");

        assertThat(result)
                .hasSize(2)
                .containsExactly(airport1, airport2);

        verify(colombiaApiRestClient).get();
        verify(requestHeadersUriSpec)
                .uri("/Airport/search/{keyword}", "Colombia");
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec)
                .body(ColombiaAirportResponse[].class);
    }

    @Test
    void shouldReturnEmptyListWhenFindByKeywordReturnsNull() {
        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(
                eq("/Airport/search/{keyword}"),
                eq("BOG")
        )).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(null);

        List<ColombiaAirportResponse> result =
                adapter.findByKeyword("BOG");

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenFindByKeywordReturnsEmptyArray() {
        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(
                eq("/Airport/search/{keyword}"),
                eq("BOG")
        )).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(new ColombiaAirportResponse[0]);

        List<ColombiaAirportResponse> result =
                adapter.findByKeyword("BOG");

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void shouldReturnFirstMatchingIataAirportWhenMultipleResultsHaveSameCode() {
        ColombiaAirportResponse first =
                createAirport(1L, "El Dorado", "BOG", "SKBO");

        ColombiaAirportResponse second =
                createAirport(2L, "El Dorado Alternativo", "BOG", "SKBO");

        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(
                eq("/Airport/search/{keyword}"),
                eq("BOG")
        )).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(new ColombiaAirportResponse[]{
                        first,
                        second
                });

        ColombiaAirportResponse result =
                adapter.findByIataCode("BOG");

        assertThat(result)
                .isEqualTo(first);
    }

    @Test
    void shouldReturnNullWhenAirportIataCodeIsNull() {
        ColombiaAirportResponse airport =
                createAirport(
                        1L,
                        "Aeropuerto sin IATA",
                        null,
                        "SKXX"
                );

        when(colombiaApiRestClient.get())
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(
                eq("/Airport/search/{keyword}"),
                eq("BOG")
        )).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(ColombiaAirportResponse[].class))
                .thenReturn(new ColombiaAirportResponse[]{
                        airport
                });

        ColombiaAirportResponse result =
                adapter.findByIataCode("BOG");

        assertThat(result)
                .isNull();
    }

    private ColombiaAirportResponse createAirport(
            Long id,
            String name,
            String iataCode,
            String oaciCode
    ) {
        return new ColombiaAirportResponse(
                id,
                name,
                iataCode,
                oaciCode,
                "airport",
                1L,
                null,
                1L,
                null,
                4.7016,
                -74.1469
        );
    }
}