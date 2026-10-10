package com.itinerarios.airport.controller;

import com.itinerarios.airport.dto.AirportDto;
import com.itinerarios.airport.exception.AirportNotFoundException;
import com.itinerarios.airport.exception.ExternalProviderException;
import com.itinerarios.airport.service.AirportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Tests de API (seccion 37): codigos HTTP y JSON de error (seccion 64). */
@WebMvcTest(AirportController.class)
class AirportControllerTest {

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

        mockMvc.perform(get("/api/airports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].icaoCode").value("SKBO"));
    }

    @Test
    void getByIata_devuelve200() throws Exception {
        when(airportService.findByIataCode("BOG")).thenReturn(bog());

        mockMvc.perform(get("/api/airports/iata/BOG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode").value("BOG"));
    }

    @Test
    void getByIata_inexistente_devuelve404ConJsonDeError() throws Exception {
        when(airportService.findByIataCode("XYZ")).thenThrow(AirportNotFoundException.byIataCode("XYZ"));

        mockMvc.perform(get("/api/airports/iata/XYZ").header("X-Correlation-ID", "abc-123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/airports/iata/XYZ"))
                .andExpect(jsonPath("$.correlationId").value("abc-123"));
    }

    @Test
    void getById_noNumerico_devuelve400() throws Exception {
        mockMvc.perform(get("/api/airports/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void proveedorExternoCaido_devuelve502() throws Exception {
        when(airportService.findByIataCode("BOG")).thenThrow(new ExternalProviderException("API Colombia down"));

        mockMvc.perform(get("/api/airports/iata/BOG"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("EXTERNAL_PROVIDER_ERROR"));
    }

    @Test
    void rutaInexistente_devuelve404NoUn500() throws Exception {
        mockMvc.perform(get("/api/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
