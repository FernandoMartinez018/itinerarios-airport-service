package com.itinerarios.airport.mapper;

import com.itinerarios.airport.dto.AirportDto;
import com.itinerarios.airport.dto.adapter.ColombiaAirportResponse;
import com.itinerarios.airport.dto.adapter.ColombiaCityRef;
import com.itinerarios.airport.dto.adapter.ColombiaDepartmentRef;
import com.itinerarios.airport.entity.Airport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ColombiaAirportMapperTest {

    private ColombiaAirportMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ColombiaAirportMapper();
    }

    @Test
    void shouldMapExternalResponseToEntityWithAllValues() {

        ColombiaAirportResponse external = new ColombiaAirportResponse(
                1L,
                "Aeropuerto Internacional El Dorado",
                "BOG",
                "SKBO",
                "Aeropuerto",
                25L,
                new ColombiaDepartmentRef(
                        25L,
                        "Cundinamarca"
                ),
                25001L,
                new ColombiaCityRef(
                        25001L,
                        "Bogotá"
                ),
                4.7016,
                -74.1469
        );

        Airport airport = mapper.toEntity(external);

        assertThat(airport).isNotNull();

        assertThat(airport.getId())
                .isNull();

        assertThat(airport.getExternalId())
                .isEqualTo("1");

        assertThat(airport.getIataCode())
                .isEqualTo("BOG");

        assertThat(airport.getIcaoCode())
                .isEqualTo("SKBO");

        assertThat(airport.getName())
                .isEqualTo("Aeropuerto Internacional El Dorado");

        assertThat(airport.getCity())
                .isEqualTo("Bogotá");

        assertThat(airport.getDepartment())
                .isEqualTo("Cundinamarca");

        assertThat(airport.getLatitude())
                .isEqualTo(BigDecimal.valueOf(4.7016));

        assertThat(airport.getLongitude())
                .isEqualTo(BigDecimal.valueOf(-74.1469));

        assertThat(airport.isActive())
                .isTrue();
    }

    @Test
    void shouldConvertBlankCodesToNull() {

        ColombiaAirportResponse external = new ColombiaAirportResponse(
                1L,
                "Aeropuerto de prueba",
                "",
                "   ",
                "Aeropuerto",
                1L,
                null,
                2L,
                null,
                1.0,
                2.0
        );

        Airport airport = mapper.toEntity(external);

        assertThat(airport.getIataCode())
                .isNull();

        assertThat(airport.getIcaoCode())
                .isNull();
    }

    @Test
    void shouldKeepValidCodesUnchanged() {

        ColombiaAirportResponse external = new ColombiaAirportResponse(
                1L,
                "Aeropuerto de prueba",
                " BOG ",
                " SKBO ",
                "Aeropuerto",
                1L,
                null,
                2L,
                null,
                1.0,
                2.0
        );

        Airport airport = mapper.toEntity(external);

        /*
         * blankToNull únicamente transforma valores
         * completamente vacíos. No hace trim().
         */
        assertThat(airport.getIataCode())
                .isEqualTo(" BOG ");

        assertThat(airport.getIcaoCode())
                .isEqualTo(" SKBO ");
    }

    @Test
    void shouldHandleNullCityAndDepartment() {

        ColombiaAirportResponse external = new ColombiaAirportResponse(
                1L,
                "Aeropuerto de prueba",
                "BOG",
                "SKBO",
                "Aeropuerto",
                1L,
                null,
                2L,
                null,
                4.0,
                -74.0
        );

        Airport airport = mapper.toEntity(external);

        assertThat(airport.getCity())
                .isNull();

        assertThat(airport.getDepartment())
                .isNull();
    }

    @Test
    void shouldHandleNullCoordinates() {

        ColombiaAirportResponse external = new ColombiaAirportResponse(
                1L,
                "Aeropuerto de prueba",
                "BOG",
                "SKBO",
                "Aeropuerto",
                1L,
                null,
                2L,
                null,
                null,
                null
        );

        Airport airport = mapper.toEntity(external);

        assertThat(airport.getLatitude())
                .isNull();

        assertThat(airport.getLongitude())
                .isNull();
    }

    @Test
    void shouldHandleNullIataAndOaciCodes() {

        ColombiaAirportResponse external = new ColombiaAirportResponse(
                1L,
                "Aeropuerto de prueba",
                null,
                null,
                "Aeropuerto",
                1L,
                null,
                2L,
                null,
                4.0,
                -74.0
        );

        Airport airport = mapper.toEntity(external);

        assertThat(airport.getIataCode())
                .isNull();

        assertThat(airport.getIcaoCode())
                .isNull();
    }

    @Test
    void shouldMapExternalResponseToExistingEntity() {

        Airport airport = new Airport(
                "1",
                "OLD",
                "SKOLD",
                "Nombre anterior",
                "Ciudad anterior",
                "Departamento anterior",
                new BigDecimal("1.0000000"),
                new BigDecimal("2.0000000")
        );

        ColombiaAirportResponse external = new ColombiaAirportResponse(
                1L,
                "Aeropuerto Internacional El Dorado",
                "BOG",
                "SKBO",
                "Aeropuerto",
                25L,
                new ColombiaDepartmentRef(
                        25L,
                        "Cundinamarca"
                ),
                25001L,
                new ColombiaCityRef(
                        25001L,
                        "Bogotá"
                ),
                4.7016,
                -74.1469
        );

        mapper.updateEntity(airport, external);

        assertThat(airport.getExternalId())
                .isEqualTo("1");

        assertThat(airport.getIataCode())
                .isEqualTo("BOG");

        assertThat(airport.getIcaoCode())
                .isEqualTo("SKBO");

        assertThat(airport.getName())
                .isEqualTo("Aeropuerto Internacional El Dorado");

        assertThat(airport.getCity())
                .isEqualTo("Bogotá");

        assertThat(airport.getDepartment())
                .isEqualTo("Cundinamarca");

        assertThat(airport.getLatitude())
                .isEqualTo(BigDecimal.valueOf(4.7016));

        assertThat(airport.getLongitude())
                .isEqualTo(BigDecimal.valueOf(-74.1469));
    }

    @Test
    void shouldUpdateExistingEntityWithNullOptionalValues() {

        Airport airport = new Airport(
                "1",
                "BOG",
                "SKBO",
                "Aeropuerto anterior",
                "Bogotá",
                "Cundinamarca",
                new BigDecimal("4.7016000"),
                new BigDecimal("-74.1469000")
        );

        ColombiaAirportResponse external = new ColombiaAirportResponse(
                1L,
                "Aeropuerto actualizado",
                "",
                "   ",
                "Aeropuerto",
                25L,
                null,
                25001L,
                null,
                null,
                null
        );

        mapper.updateEntity(airport, external);

        assertThat(airport.getIataCode())
                .isNull();

        assertThat(airport.getIcaoCode())
                .isNull();

        assertThat(airport.getName())
                .isEqualTo("Aeropuerto actualizado");

        assertThat(airport.getCity())
                .isNull();

        assertThat(airport.getDepartment())
                .isNull();

        assertThat(airport.getLatitude())
                .isNull();

        assertThat(airport.getLongitude())
                .isNull();
    }

    @Test
    void shouldUpdateEntityWithValidCodes() {

        Airport airport = createAirport();

        ColombiaAirportResponse external = new ColombiaAirportResponse(
                1L,
                "Aeropuerto actualizado",
                "MDE",
                "SKRG",
                "Aeropuerto",
                5L,
                new ColombiaDepartmentRef(
                        5L,
                        "Antioquia"
                ),
                50001L,
                new ColombiaCityRef(
                        50001L,
                        "Rionegro"
                ),
                6.1645,
                -75.4231
        );

        mapper.updateEntity(airport, external);

        assertThat(airport.getIataCode())
                .isEqualTo("MDE");

        assertThat(airport.getIcaoCode())
                .isEqualTo("SKRG");

        assertThat(airport.getName())
                .isEqualTo("Aeropuerto actualizado");

        assertThat(airport.getCity())
                .isEqualTo("Rionegro");

        assertThat(airport.getDepartment())
                .isEqualTo("Antioquia");

        assertThat(airport.getLatitude())
                .isEqualTo(BigDecimal.valueOf(6.1645));

        assertThat(airport.getLongitude())
                .isEqualTo(BigDecimal.valueOf(-75.4231));
    }

    @Test
    void shouldMapEntityToDtoWithAllValues() {

        Airport airport = createAirport();

        AirportDto dto = mapper.toDto(airport);

        assertThat(dto).isNotNull();

        assertThat(dto.id())
                .isNull();

        assertThat(dto.externalId())
                .isEqualTo("CO-BOG-001");

        assertThat(dto.iataCode())
                .isEqualTo("BOG");

        assertThat(dto.icaoCode())
                .isEqualTo("SKBO");

        assertThat(dto.name())
                .isEqualTo("Aeropuerto Internacional El Dorado");

        assertThat(dto.city())
                .isEqualTo("Bogotá");

        assertThat(dto.department())
                .isEqualTo("Cundinamarca");

        assertThat(dto.latitude())
                .isEqualTo(4.7016);

        assertThat(dto.longitude())
                .isEqualTo(-74.1469);

        assertThat(dto.active())
                .isTrue();
    }

    @Test
    void shouldMapEntityToDtoWithNullCoordinates() {

        Airport airport = new Airport(
                "CO-TEST-001",
                "BOG",
                "SKBO",
                "Aeropuerto de prueba",
                "Bogotá",
                "Cundinamarca",
                null,
                null
        );

        AirportDto dto = mapper.toDto(airport);

        assertThat(dto.latitude())
                .isNull();

        assertThat(dto.longitude())
                .isNull();

        assertThat(dto.externalId())
                .isEqualTo("CO-TEST-001");

        assertThat(dto.iataCode())
                .isEqualTo("BOG");

        assertThat(dto.icaoCode())
                .isEqualTo("SKBO");

        assertThat(dto.active())
                .isTrue();
    }

    @Test
    void shouldMapInactiveEntityToDto() {

        Airport airport = createAirport();

        airport.setActive(false);

        AirportDto dto = mapper.toDto(airport);

        assertThat(dto.active())
                .isFalse();
    }

    @Test
    void shouldMapEntityToDtoWithNullValues() {

        Airport airport = new Airport(
                "CO-TEST-001",
                null,
                null,
                "Aeropuerto de prueba",
                "Bogotá",
                null,
                null,
                null
        );

        AirportDto dto = mapper.toDto(airport);

        assertThat(dto.externalId())
                .isEqualTo("CO-TEST-001");

        assertThat(dto.iataCode())
                .isNull();

        assertThat(dto.icaoCode())
                .isNull();

        assertThat(dto.name())
                .isEqualTo("Aeropuerto de prueba");

        assertThat(dto.city())
                .isEqualTo("Bogotá");

        assertThat(dto.department())
                .isNull();

        assertThat(dto.latitude())
                .isNull();

        assertThat(dto.longitude())
                .isNull();

        assertThat(dto.active())
                .isTrue();
    }

    @Test
    void shouldConvertBigDecimalCoordinatesToDouble() {

        Airport airport = new Airport(
                "CO-TEST-001",
                "BOG",
                "SKBO",
                "Aeropuerto de prueba",
                "Bogotá",
                "Cundinamarca",
                new BigDecimal("4.7016000"),
                new BigDecimal("-74.1469000")
        );

        AirportDto dto = mapper.toDto(airport);

        assertThat(dto.latitude())
                .isEqualTo(4.7016);

        assertThat(dto.longitude())
                .isEqualTo(-74.1469);
    }

    @Test
    void shouldPreserveExternalIdWhenUpdatingEntity() {

        Airport airport = createAirport();

        ColombiaAirportResponse external = new ColombiaAirportResponse(
                999L,
                "Nuevo aeropuerto",
                "NUE",
                "SKXX",
                "Aeropuerto",
                1L,
                null,
                2L,
                null,
                1.0,
                2.0
        );

        mapper.updateEntity(airport, external);

        /*
         * updateEntity actualiza los datos del aeropuerto,
         * pero NO modifica externalId.
         */
        assertThat(airport.getExternalId())
                .isEqualTo("CO-BOG-001");
    }

    private Airport createAirport() {

        return new Airport(
                "CO-BOG-001",
                "BOG",
                "SKBO",
                "Aeropuerto Internacional El Dorado",
                "Bogotá",
                "Cundinamarca",
                new BigDecimal("4.7016000"),
                new BigDecimal("-74.1469000")
        );
    }
}