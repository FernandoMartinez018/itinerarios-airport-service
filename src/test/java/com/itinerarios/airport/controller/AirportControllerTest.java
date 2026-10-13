package com.itinerarios.airport.controller;

import com.itinerarios.airport.config.SecurityConfig;
import com.itinerarios.airport.dto.AirportDto;
import com.itinerarios.airport.exception.AirportNotFoundException;
import com.itinerarios.airport.exception.ExternalProviderException;
import com.itinerarios.airport.service.AirportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Tests de API (seccion 37): codigos HTTP y JSON de error (seccion 64). */
@WebMvcTest(AirportController.class)
@Import(SecurityConfig.class)
class AirportControllerTest {

    /** JWT de servicio con el scope que Keycloak da al Gateway y a Itinerary Service. */
    private static JwtRequestPostProcessor reader() {
        return jwt().authorities(new SimpleGrantedAuthority("SCOPE_airport.read"));
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AirportService airportService;

    private AirportDto bog() {
        return new AirportDto(1L, "1", "BOG", "SKBO", "El Dorado", "Bogota", "Cundinamarca",
                4.7016, -74.1469, true);
    }

    @Test
    void getAll_devuelve200() throws Exception {
        when(airportService.findAll()).thenReturn(List.of(bog()));

        mockMvc.perform(get("/api/airports").with(reader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].icaoCode").value("SKBO"));
    }

    @Test
    void getByIata_devuelve200() throws Exception {
        when(airportService.findByIataCode("BOG")).thenReturn(bog());

        mockMvc.perform(get("/api/airports/iata/BOG").with(reader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode").value("BOG"));
    }

    @Test
    void getByIata_inexistente_devuelve404ConJsonDeError() throws Exception {
        when(airportService.findByIataCode("XYZ")).thenThrow(AirportNotFoundException.byIataCode("XYZ"));

        mockMvc.perform(get("/api/airports/iata/XYZ").with(reader()).header("X-Correlation-ID", "abc-123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/airports/iata/XYZ"))
                .andExpect(jsonPath("$.correlationId").value("abc-123"));
    }

    @Test
    void getById_noNumerico_devuelve400() throws Exception {
        mockMvc.perform(get("/api/airports/abc").with(reader()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void proveedorExternoCaido_devuelve502() throws Exception {
        when(airportService.findByIataCode("BOG")).thenThrow(new ExternalProviderException("API Colombia down"));

        mockMvc.perform(get("/api/airports/iata/BOG").with(reader()))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("EXTERNAL_PROVIDER_ERROR"));
    }

    @Test
    void rutaInexistente_devuelve404NoUn500() throws Exception {
        mockMvc.perform(get("/api/no-existe").with(reader()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ---- Seguridad S2S (paso 31) ----

    @Test
    void sinToken_devuelve401ConJsonDeError() throws Exception {
        mockMvc.perform(get("/api/airports/iata/BOG").header("X-Correlation-ID", "sec-001"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.correlationId").value("sec-001"));
    }

    @Test
    void tokenSinScopeAirportRead_devuelve403() throws Exception {
        mockMvc.perform(get("/api/airports/iata/BOG").with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_itinerary.read"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void actuatorQuedaAbiertoParaHealthChecks() throws Exception {
        // Sin token: no debe ser 401/403 (el endpoint no existe en el slice, por eso 404).
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isNotFound());
    }
}
