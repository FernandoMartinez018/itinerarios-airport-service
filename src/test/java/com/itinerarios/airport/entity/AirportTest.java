package com.itinerarios.airport.entity;

import com.itinerarios.airport.entity.Airport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AirportTest {

    @Test
    void shouldCreateAirportWithAllValues() {

        BigDecimal latitude = new BigDecimal("4.7016000");
        BigDecimal longitude = new BigDecimal("-74.1469000");

        Airport airport = new Airport(
                "CO-BOG-001",
                "BOG",
                "SKBO",
                "Aeropuerto Internacional El Dorado",
                "Bogotá",
                "Cundinamarca",
                latitude,
                longitude
        );

        assertThat(airport.getId()).isNull();

        assertThat(airport.getExternalId())
                .isEqualTo("CO-BOG-001");

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
                .isEqualTo(latitude);

        assertThat(airport.getLongitude())
                .isEqualTo(longitude);

        assertThat(airport.isActive())
                .isTrue();

        assertThat(airport.getCreatedAt())
                .isNull();

        assertThat(airport.getUpdatedAt())
                .isNull();
    }

    @Test
    void shouldCreateAirportWithNullableOptionalFields() {

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

        assertThat(airport.getExternalId())
                .isEqualTo("CO-TEST-001");

        assertThat(airport.getIataCode())
                .isNull();

        assertThat(airport.getIcaoCode())
                .isNull();

        assertThat(airport.getName())
                .isEqualTo("Aeropuerto de prueba");

        assertThat(airport.getCity())
                .isEqualTo("Bogotá");

        assertThat(airport.getDepartment())
                .isNull();

        assertThat(airport.getLatitude())
                .isNull();

        assertThat(airport.getLongitude())
                .isNull();

        assertThat(airport.isActive())
                .isTrue();
    }

    @Test
    void shouldCreateAirportUsingJpaProtectedConstructor() {

        Airport airport = new Airport();

        assertThat(airport.getId()).isNull();
        assertThat(airport.getExternalId()).isNull();
        assertThat(airport.getIataCode()).isNull();
        assertThat(airport.getIcaoCode()).isNull();
        assertThat(airport.getName()).isNull();
        assertThat(airport.getCity()).isNull();
        assertThat(airport.getDepartment()).isNull();
        assertThat(airport.getLatitude()).isNull();
        assertThat(airport.getLongitude()).isNull();

        /*
         * El valor inicial del atributo active es true.
         */
        assertThat(airport.isActive()).isTrue();

        assertThat(airport.getCreatedAt()).isNull();
        assertThat(airport.getUpdatedAt()).isNull();
    }

    @Test
    void shouldSetAndGetExternalId() {

        Airport airport = createAirport();

        airport.setExternalId("CO-MDE-001");

        assertThat(airport.getExternalId())
                .isEqualTo("CO-MDE-001");
    }

    @Test
    void shouldSetAndGetIataCode() {

        Airport airport = createAirport();

        airport.setIataCode("MDE");

        assertThat(airport.getIataCode())
                .isEqualTo("MDE");
    }

    @Test
    void shouldSetAndGetIcaoCode() {

        Airport airport = createAirport();

        airport.setIcaoCode("SKRG");

        assertThat(airport.getIcaoCode())
                .isEqualTo("SKRG");
    }

    @Test
    void shouldSetAndGetName() {

        Airport airport = createAirport();

        airport.setName("Aeropuerto Internacional José María Córdova");

        assertThat(airport.getName())
                .isEqualTo("Aeropuerto Internacional José María Córdova");
    }

    @Test
    void shouldSetAndGetCity() {

        Airport airport = createAirport();

        airport.setCity("Rionegro");

        assertThat(airport.getCity())
                .isEqualTo("Rionegro");
    }

    @Test
    void shouldSetAndGetDepartment() {

        Airport airport = createAirport();

        airport.setDepartment("Antioquia");

        assertThat(airport.getDepartment())
                .isEqualTo("Antioquia");
    }

    @Test
    void shouldSetAndGetLatitude() {

        Airport airport = createAirport();

        BigDecimal latitude = new BigDecimal("6.1645000");

        airport.setLatitude(latitude);

        assertThat(airport.getLatitude())
                .isEqualTo(latitude);
    }

    @Test
    void shouldSetAndGetLongitude() {

        Airport airport = createAirport();

        BigDecimal longitude = new BigDecimal("-75.4231000");

        airport.setLongitude(longitude);

        assertThat(airport.getLongitude())
                .isEqualTo(longitude);
    }

    @Test
    void shouldSetAndGetActive() {

        Airport airport = createAirport();

        assertThat(airport.isActive())
                .isTrue();

        airport.setActive(false);

        assertThat(airport.isActive())
                .isFalse();

        airport.setActive(true);

        assertThat(airport.isActive())
                .isTrue();
    }

    @Test
    void shouldExecuteOnCreateLifecycleCallback() {

        Airport airport = createAirport();

        assertThat(airport.getCreatedAt())
                .isNull();

        assertThat(airport.getUpdatedAt())
                .isNull();

        airport.onCreate();

        assertThat(airport.getCreatedAt())
                .isNotNull();

        assertThat(airport.getUpdatedAt())
                .isNotNull();

        assertThat(airport.getCreatedAt())
                .isEqualTo(airport.getUpdatedAt());

        assertThat(airport.getCreatedAt())
                .isBeforeOrEqualTo(OffsetDateTime.now());

        assertThat(airport.getUpdatedAt())
                .isBeforeOrEqualTo(OffsetDateTime.now());
    }

    @Test
    void shouldExecuteOnUpdateLifecycleCallback() {

        Airport airport = createAirport();

        airport.onCreate();

        OffsetDateTime originalCreatedAt =
                airport.getCreatedAt();

        OffsetDateTime originalUpdatedAt =
                airport.getUpdatedAt();

        airport.onUpdate();

        assertThat(airport.getCreatedAt())
                .isEqualTo(originalCreatedAt);

        assertThat(airport.getUpdatedAt())
                .isNotNull();

        assertThat(airport.getUpdatedAt())
                .isAfterOrEqualTo(originalUpdatedAt);
    }

    @Test
    void shouldUpdateOnlyUpdatedAtWhenOnUpdateIsExecuted() {

        Airport airport = createAirport();

        airport.onCreate();

        OffsetDateTime createdAt =
                airport.getCreatedAt();

        OffsetDateTime firstUpdatedAt =
                airport.getUpdatedAt();

        airport.setName("Nombre actualizado");

        airport.onUpdate();

        assertThat(airport.getCreatedAt())
                .isEqualTo(createdAt);

        assertThat(airport.getUpdatedAt())
                .isNotNull();

        assertThat(airport.getUpdatedAt())
                .isAfterOrEqualTo(firstUpdatedAt);

        assertThat(airport.getName())
                .isEqualTo("Nombre actualizado");
    }

    @Test
    void shouldAllowSettingNullableFieldsToNull() {

        Airport airport = createAirport();

        airport.setExternalId(null);
        airport.setIataCode(null);
        airport.setIcaoCode(null);
        airport.setName(null);
        airport.setCity(null);
        airport.setDepartment(null);
        airport.setLatitude(null);
        airport.setLongitude(null);

        assertThat(airport.getExternalId()).isNull();
        assertThat(airport.getIataCode()).isNull();
        assertThat(airport.getIcaoCode()).isNull();
        assertThat(airport.getName()).isNull();
        assertThat(airport.getCity()).isNull();
        assertThat(airport.getDepartment()).isNull();
        assertThat(airport.getLatitude()).isNull();
        assertThat(airport.getLongitude()).isNull();
    }

    /**
     * Crea una entidad Airport con valores válidos
     * para reutilizarla en las pruebas.
     */
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