package com.itinerarios.airport.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itinerarios.airport.dto.AirportDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AirportDtoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateAirportDtoWithCorrectValues() {

        AirportDto airport = new AirportDto(
                1L,
                "CO-BOG-001",
                "BOG",
                "SKBO",
                "Aeropuerto Internacional El Dorado",
                "Bogotá",
                "Cundinamarca",
                4.7016,
                -74.1469,
                true
        );

        assertThat(airport.id()).isEqualTo(1L);
        assertThat(airport.externalId()).isEqualTo("CO-BOG-001");
        assertThat(airport.iataCode()).isEqualTo("BOG");
        assertThat(airport.icaoCode()).isEqualTo("SKBO");
        assertThat(airport.name())
                .isEqualTo("Aeropuerto Internacional El Dorado");
        assertThat(airport.city()).isEqualTo("Bogotá");
        assertThat(airport.department()).isEqualTo("Cundinamarca");
        assertThat(airport.latitude()).isEqualTo(4.7016);
        assertThat(airport.longitude()).isEqualTo(-74.1469);
        assertThat(airport.active()).isTrue();
    }

    @Test
    void shouldDeserializeJsonCorrectly() throws Exception {

        String json = """
                {
                    "id": 1,
                    "externalId": "CO-BOG-001",
                    "iataCode": "BOG",
                    "icaoCode": "SKBO",
                    "name": "Aeropuerto Internacional El Dorado",
                    "city": "Bogotá",
                    "department": "Cundinamarca",
                    "latitude": 4.7016,
                    "longitude": -74.1469,
                    "active": true
                }
                """;

        AirportDto airport =
                objectMapper.readValue(json, AirportDto.class);

        assertThat(airport.id()).isEqualTo(1L);
        assertThat(airport.externalId()).isEqualTo("CO-BOG-001");
        assertThat(airport.iataCode()).isEqualTo("BOG");
        assertThat(airport.icaoCode()).isEqualTo("SKBO");
        assertThat(airport.name())
                .isEqualTo("Aeropuerto Internacional El Dorado");
        assertThat(airport.city()).isEqualTo("Bogotá");
        assertThat(airport.department()).isEqualTo("Cundinamarca");
        assertThat(airport.latitude()).isEqualTo(4.7016);
        assertThat(airport.longitude()).isEqualTo(-74.1469);
        assertThat(airport.active()).isTrue();
    }

    @Test
    void shouldDeserializeInactiveAirportCorrectly() throws Exception {

        String json = """
                {
                    "id": 2,
                    "externalId": "CO-CLO-001",
                    "iataCode": "CLO",
                    "icaoCode": "SKCL",
                    "name": "Aeropuerto Internacional Alfonso Bonilla Aragón",
                    "city": "Palmira",
                    "department": "Valle del Cauca",
                    "latitude": 3.5432,
                    "longitude": -76.3816,
                    "active": false
                }
                """;

        AirportDto airport =
                objectMapper.readValue(json, AirportDto.class);

        assertThat(airport.id()).isEqualTo(2L);
        assertThat(airport.iataCode()).isEqualTo("CLO");
        assertThat(airport.icaoCode()).isEqualTo("SKCL");
        assertThat(airport.active()).isFalse();
    }

    @Test
    void shouldAllowNullValuesForReferenceFields() throws Exception {

        String json = """
                {
                    "id": null,
                    "externalId": null,
                    "iataCode": null,
                    "icaoCode": null,
                    "name": null,
                    "city": null,
                    "department": null,
                    "latitude": null,
                    "longitude": null,
                    "active": false
                }
                """;

        AirportDto airport =
                objectMapper.readValue(json, AirportDto.class);

        assertThat(airport.id()).isNull();
        assertThat(airport.externalId()).isNull();
        assertThat(airport.iataCode()).isNull();
        assertThat(airport.icaoCode()).isNull();
        assertThat(airport.name()).isNull();
        assertThat(airport.city()).isNull();
        assertThat(airport.department()).isNull();
        assertThat(airport.latitude()).isNull();
        assertThat(airport.longitude()).isNull();

        assertThat(airport.active()).isFalse();
    }

    @Test
    void shouldSerializeCorrectly() throws Exception {

        AirportDto airport = new AirportDto(
                1L,
                "CO-BOG-001",
                "BOG",
                "SKBO",
                "Aeropuerto Internacional El Dorado",
                "Bogotá",
                "Cundinamarca",
                4.7016,
                -74.1469,
                true
        );

        String json = objectMapper.writeValueAsString(airport);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"externalId\":\"CO-BOG-001\"");
        assertThat(json).contains("\"iataCode\":\"BOG\"");
        assertThat(json).contains("\"icaoCode\":\"SKBO\"");
        assertThat(json)
                .contains("\"name\":\"Aeropuerto Internacional El Dorado\"");
        assertThat(json).contains("\"city\":\"Bogotá\"");
        assertThat(json).contains("\"department\":\"Cundinamarca\"");
        assertThat(json).contains("\"latitude\":4.7016");
        assertThat(json).contains("\"longitude\":-74.1469");
        assertThat(json).contains("\"active\":true");
    }
}