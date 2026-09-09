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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AirportServiceTest {

    private AirportRepository airportRepository;
    private ColombiaAirportAdapter colombiaAirportAdapter;
    private ColombiaAirportMapper mapper;

    private AirportService airportService;

    @BeforeEach
    void setUp() {
        airportRepository = mock(AirportRepository.class);
        colombiaAirportAdapter = mock(ColombiaAirportAdapter.class);
        mapper = mock(ColombiaAirportMapper.class);

        airportService = new AirportService(
                airportRepository,
                colombiaAirportAdapter,
                mapper
        );
    }

    @Test
    void shouldFindAllAirports() {
        Airport airport1 = createAirport(
                "1",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca"
        );

        Airport airport2 = createAirport(
                "2",
                "MDE",
                "SKRG",
                "José María Córdova",
                "Rionegro",
                "Antioquia"
        );

        AirportDto dto1 = createDto(
                1L,
                "1",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca",
                true
        );

        AirportDto dto2 = createDto(
                2L,
                "2",
                "MDE",
                "SKRG",
                "José María Córdova",
                "Rionegro",
                "Antioquia",
                true
        );

        when(airportRepository.findAll())
                .thenReturn(List.of(airport1, airport2));

        when(mapper.toDto(airport1)).thenReturn(dto1);
        when(mapper.toDto(airport2)).thenReturn(dto2);

        List<AirportDto> result = airportService.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactly(dto1, dto2);

        verify(airportRepository).findAll();
        verify(mapper).toDto(airport1);
        verify(mapper).toDto(airport2);
    }

    @Test
    void shouldReturnEmptyListWhenNoAirportsExist() {
        when(airportRepository.findAll())
                .thenReturn(List.of());

        List<AirportDto> result = airportService.findAll();

        assertThat(result)
                .isNotNull()
                .isEmpty();

        verify(airportRepository).findAll();
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldFindAirportById() {
        Long id = 1L;

        Airport airport = createAirport(
                "1",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca"
        );

        AirportDto dto = createDto(
                id,
                "1",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca",
                true
        );

        when(airportRepository.findById(id))
                .thenReturn(Optional.of(airport));

        when(mapper.toDto(airport))
                .thenReturn(dto);

        AirportDto result = airportService.findById(id);

        assertThat(result).isEqualTo(dto);

        verify(airportRepository).findById(id);
        verify(mapper).toDto(airport);
    }

    @Test
    void shouldThrowAirportNotFoundExceptionWhenIdDoesNotExist() {
        Long id = 999L;

        when(airportRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> airportService.findById(id))
                .isInstanceOf(AirportNotFoundException.class);

        verify(airportRepository).findById(id);
        verifyNoInteractions(mapper);
        verifyNoInteractions(colombiaAirportAdapter);
    }

    @Test
    void shouldFindAirportByIataCodeFromDatabase() {
        String iataCode = "BOG";

        Airport airport = createAirport(
                "1",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca"
        );

        AirportDto dto = createDto(
                1L,
                "1",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca",
                true
        );

        when(airportRepository.findByIataCodeIgnoreCase(iataCode))
                .thenReturn(Optional.of(airport));

        when(mapper.toDto(airport))
                .thenReturn(dto);

        AirportDto result = airportService.findByIataCode(iataCode);

        assertThat(result).isEqualTo(dto);

        verify(airportRepository).findByIataCodeIgnoreCase(iataCode);
        verify(mapper).toDto(airport);

        verifyNoInteractions(colombiaAirportAdapter);
        verify(airportRepository, never()).save(any());
    }

    @Test
    void shouldFetchAirportFromExternalProviderWhenNotFoundLocally() {
        String iataCode = "BOG";

        ColombiaAirportResponse external = createExternalResponse(
                123L,
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca",
                4.7016,
                -74.1469
        );

        Airport airport = createAirport(
                "123",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca"
        );

        AirportDto dto = createDto(
                null,
                "123",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca",
                true
        );

        when(airportRepository.findByIataCodeIgnoreCase(iataCode))
                .thenReturn(Optional.empty());

        when(colombiaAirportAdapter.findByIataCode(iataCode))
                .thenReturn(external);

        when(airportRepository.findByExternalId("123"))
                .thenReturn(Optional.empty());

        when(mapper.toEntity(external))
                .thenReturn(airport);

        when(airportRepository.save(airport))
                .thenReturn(airport);

        when(mapper.toDto(airport))
                .thenReturn(dto);

        AirportDto result = airportService.findByIataCode(iataCode);

        assertThat(result).isEqualTo(dto);

        verify(airportRepository).findByIataCodeIgnoreCase(iataCode);
        verify(colombiaAirportAdapter).findByIataCode(iataCode);
        verify(airportRepository).findByExternalId("123");
        verify(mapper).toEntity(external);
        verify(airportRepository).save(airport);
        verify(mapper).toDto(airport);

        verify(mapper, never()).updateEntity(any(), any());
    }

    @Test
    void shouldUpdateExistingAirportWhenExternalAirportAlreadyExists() {
        String iataCode = "BOG";

        ColombiaAirportResponse external = createExternalResponse(
                123L,
                "BOG",
                "SKBO",
                "El Dorado actualizado",
                "Bogotá",
                "Cundinamarca",
                4.7016,
                -74.1469
        );

        Airport existingAirport = createAirport(
                "123",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca"
        );

        AirportDto dto = createDto(
                1L,
                "123",
                "BOG",
                "SKBO",
                "El Dorado actualizado",
                "Bogotá",
                "Cundinamarca",
                true
        );

        when(airportRepository.findByIataCodeIgnoreCase(iataCode))
                .thenReturn(Optional.empty());

        when(colombiaAirportAdapter.findByIataCode(iataCode))
                .thenReturn(external);

        when(airportRepository.findByExternalId("123"))
                .thenReturn(Optional.of(existingAirport));

        when(airportRepository.save(existingAirport))
                .thenReturn(existingAirport);

        when(mapper.toDto(existingAirport))
                .thenReturn(dto);

        AirportDto result = airportService.findByIataCode(iataCode);

        assertThat(result).isEqualTo(dto);

        verify(airportRepository).findByIataCodeIgnoreCase(iataCode);
        verify(colombiaAirportAdapter).findByIataCode(iataCode);
        verify(airportRepository).findByExternalId("123");

        verify(mapper).updateEntity(existingAirport, external);
        verify(airportRepository).save(existingAirport);
        verify(mapper).toDto(existingAirport);

        verify(mapper, never()).toEntity(external);
    }

    @Test
    void shouldThrowAirportNotFoundExceptionWhenExternalProviderReturnsNull() {
        String iataCode = "XXX";

        when(airportRepository.findByIataCodeIgnoreCase(iataCode))
                .thenReturn(Optional.empty());

        when(colombiaAirportAdapter.findByIataCode(iataCode))
                .thenReturn(null);

        assertThatThrownBy(() -> airportService.findByIataCode(iataCode))
                .isInstanceOf(AirportNotFoundException.class);

        verify(airportRepository).findByIataCodeIgnoreCase(iataCode);
        verify(colombiaAirportAdapter).findByIataCode(iataCode);

        verify(airportRepository, never()).findByExternalId(any());
        verify(airportRepository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldUseExternalIdFromProviderWhenSearchingExistingAirport() {
        String iataCode = "MDE";

        ColombiaAirportResponse external = createExternalResponse(
                456L,
                "MDE",
                "SKRG",
                "José María Córdova",
                "Rionegro",
                "Antioquia",
                6.1645,
                -75.4231
        );

        Airport airport = createAirport(
                "456",
                "MDE",
                "SKRG",
                "José María Córdova",
                "Rionegro",
                "Antioquia"
        );

        AirportDto dto = createDto(
                2L,
                "456",
                "MDE",
                "SKRG",
                "José María Córdova",
                "Rionegro",
                "Antioquia",
                true
        );

        when(airportRepository.findByIataCodeIgnoreCase(iataCode))
                .thenReturn(Optional.empty());

        when(colombiaAirportAdapter.findByIataCode(iataCode))
                .thenReturn(external);

        when(airportRepository.findByExternalId("456"))
                .thenReturn(Optional.of(airport));

        when(airportRepository.save(airport))
                .thenReturn(airport);

        when(mapper.toDto(airport))
                .thenReturn(dto);

        AirportDto result = airportService.findByIataCode(iataCode);

        assertThat(result).isEqualTo(dto);

        verify(airportRepository).findByExternalId(eq(String.valueOf(external.id())));
        verify(mapper).updateEntity(airport, external);
        verify(airportRepository).save(airport);
    }

    @Test
    void shouldReturnMappedDtoFromSavedAirport() {
        String iataCode = "BOG";

        ColombiaAirportResponse external = createExternalResponse(
                123L,
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca",
                4.7016,
                -74.1469
        );

        Airport airport = createAirport(
                "123",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca"
        );

        AirportDto dto = createDto(
                10L,
                "123",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca",
                true
        );

        when(airportRepository.findByIataCodeIgnoreCase(iataCode))
                .thenReturn(Optional.empty());

        when(colombiaAirportAdapter.findByIataCode(iataCode))
                .thenReturn(external);

        when(airportRepository.findByExternalId("123"))
                .thenReturn(Optional.empty());

        when(mapper.toEntity(external))
                .thenReturn(airport);

        when(airportRepository.save(airport))
                .thenReturn(airport);

        when(mapper.toDto(airport))
                .thenReturn(dto);

        AirportDto result = airportService.findByIataCode(iataCode);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.externalId()).isEqualTo("123");
        assertThat(result.iataCode()).isEqualTo("BOG");
        assertThat(result.icaoCode()).isEqualTo("SKBO");
        assertThat(result.name()).isEqualTo("El Dorado");

        verify(mapper).toDto(airport);
    }

    @Test
    void shouldNotCallExternalProviderWhenAirportExistsLocally() {
        String iataCode = "BOG";

        Airport airport = createAirport(
                "1",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca"
        );

        AirportDto dto = createDto(
                1L,
                "1",
                "BOG",
                "SKBO",
                "El Dorado",
                "Bogotá",
                "Cundinamarca",
                true
        );

        when(airportRepository.findByIataCodeIgnoreCase(iataCode))
                .thenReturn(Optional.of(airport));

        when(mapper.toDto(airport))
                .thenReturn(dto);

        airportService.findByIataCode(iataCode);

        verify(colombiaAirportAdapter, never())
                .findByIataCode(any());

        verify(airportRepository, never())
                .findByExternalId(any());

        verify(airportRepository, never())
                .save(any());
    }

    @Test
    void shouldHandleNullIdFromExternalResponseWhenSearchingByExternalId() {
        ColombiaAirportResponse external = new ColombiaAirportResponse(
                null,
                "BOG",
                "SKBO",
                "SKBO",
                "El Dorado",
                null,
                null,
                null,
                null,
                4.7016,
                -74.1469
        );

        Airport airport = createAirport(
                "null",
                "BOG",
                "SKBO",
                "El Dorado",
                null,
                null
        );

        AirportDto dto = createDto(
                1L,
                "null",
                "BOG",
                "SKBO",
                "El Dorado",
                null,
                null,
                true
        );

        when(airportRepository.findByIataCodeIgnoreCase("BOG"))
                .thenReturn(Optional.empty());

        when(colombiaAirportAdapter.findByIataCode("BOG"))
                .thenReturn(external);

        when(airportRepository.findByExternalId("null"))
                .thenReturn(Optional.empty());

        when(mapper.toEntity(external))
                .thenReturn(airport);

        when(airportRepository.save(airport))
                .thenReturn(airport);

        when(mapper.toDto(airport))
                .thenReturn(dto);

        AirportDto result = airportService.findByIataCode("BOG");

        assertThat(result).isEqualTo(dto);

        verify(airportRepository).findByExternalId("null");
        verify(mapper).toEntity(external);
        verify(airportRepository).save(airport);
    }

    private Airport createAirport(
            String externalId,
            String iataCode,
            String icaoCode,
            String name,
            String city,
            String department
    ) {
        return new Airport(
                externalId,
                iataCode,
                icaoCode,
                name,
                city,
                department,
                BigDecimal.valueOf(4.7016),
                BigDecimal.valueOf(-74.1469)
        );
    }

    private AirportDto createDto(
            Long id,
            String externalId,
            String iataCode,
            String icaoCode,
            String name,
            String city,
            String department,
            boolean active
    ) {
        return new AirportDto(
                id,
                externalId,
                iataCode,
                icaoCode,
                name,
                city,
                department,
                4.7016,
                -74.1469,
                active
        );
    }

    private ColombiaAirportResponse createExternalResponse(
            Long id,
            String iataCode,
            String oaciCode,
            String name,
            String city,
            String department,
            Double latitude,
            Double longitude
    ) {
        return new ColombiaAirportResponse(
                id,
                name,
                iataCode,
                oaciCode,
                "airport",
                1L,
                department != null
                        ? new ColombiaDepartmentRef(1L, department)
                        : null,
                1L,
                city != null
                        ? new ColombiaCityRef(1L, city)
                        : null,
                latitude,
                longitude
        );
    }
}
