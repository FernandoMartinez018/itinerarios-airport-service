package com.itinerarios.airport.service;

import com.itinerarios.airport.adapter.ColombiaAirportAdapter;
import com.itinerarios.airport.dto.AirportDto;
import com.itinerarios.airport.dto.adapter.ColombiaAirportResponse;
import com.itinerarios.airport.dto.adapter.ColombiaCityRef;
import com.itinerarios.airport.dto.adapter.ColombiaDepartmentRef;
import com.itinerarios.airport.entity.Airport;
import com.itinerarios.airport.exception.AirportNotFoundException;
import com.itinerarios.airport.mapper.ColombiaAirportMapper;
import com.itinerarios.airport.repository.AirportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirportServiceTest {

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private ColombiaAirportAdapter colombiaAirportAdapter;

    private final ColombiaAirportMapper mapper = new ColombiaAirportMapper();

    @InjectMocks
    private AirportService airportService;

    @Test
    void findById_devuelveAeropuertoCuandoExiste() {
        Airport airport = new Airport("1", "BOG", "SKBO", "El Dorado",
                "Bogotá", "Cundinamarca", new BigDecimal("4.7016"), new BigDecimal("-74.1469"));

        when(airportRepository.findById(1L)).thenReturn(Optional.of(airport));

        AirportDto result = airportService.findById(1L);

        assertThat(result.iataCode()).isEqualTo("BOG");
        assertThat(result.icaoCode()).isEqualTo("SKBO");
    }

    @Test
    void findById_lanzaExcepcionCuandoNoExiste() {
        when(airportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> airportService.findById(99L))
                .isInstanceOf(AirportNotFoundException.class);
    }

    @Test
    void findByIataCode_consultaApiColombiaYPersisteCuandoNoExisteLocalmente() {
        when(airportRepository.findByIataCodeIgnoreCase("MDE")).thenReturn(Optional.empty());
        when(airportRepository.findByExternalId(anyString())).thenReturn(Optional.empty());

        ColombiaAirportResponse external = new ColombiaAirportResponse(
                2L, "José María Córdova", "MDE", "SKRG", "AIRPORT",
                5L, new ColombiaDepartmentRef(5L, "Antioquia"),
                10L, new ColombiaCityRef(10L, "Rionegro"),
                6.1645, -75.4231
        );
        when(colombiaAirportAdapter.findByIataCode("MDE")).thenReturn(external);
        when(airportRepository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        AirportDto result = airportService.findByIataCode("MDE");

        assertThat(result.iataCode()).isEqualTo("MDE");
        assertThat(result.icaoCode()).isEqualTo("SKRG");
        assertThat(result.city()).isEqualTo("Rionegro");
        verify(airportRepository, times(1)).save(any(Airport.class));
    }

    @Test
    void findByIataCode_lanzaExcepcionCuandoNoExisteEnNingunLado() {
        when(airportRepository.findByIataCodeIgnoreCase("XXX")).thenReturn(Optional.empty());
        when(colombiaAirportAdapter.findByIataCode("XXX")).thenReturn(null);

        assertThatThrownBy(() -> airportService.findByIataCode("XXX"))
                .isInstanceOf(AirportNotFoundException.class);
    }
}
