package com.itinerarios.airport.dto.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColombiaAirportResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateColombiaAirportResponseWithAllValues() {
        ColombiaDepartmentRef department =
                new ColombiaDepartmentRef(25L, "Cundinamarca");

        ColombiaCityRef city =
                new ColombiaCityRef(25001L, "Bogotá");

        ColombiaAirportResponse response = new ColombiaAirportResponse(
                1L,
                "Aeropuerto Internacional El Dorado",
                "BOG",
                "SKBO",
                "Aeropuerto",
                25L,
                department,
                25001L,
                city,
                4.7016,
                -74.1469
        );

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name())
                .isEqualTo("Aeropuerto Internacional El Dorado");
        assertThat(response.iataCode()).isEqualTo("BOG");
        assertThat(response.oaciCode()).isEqualTo("SKBO");
        assertThat(response.type()).isEqualTo("Aeropuerto");
        assertThat(response.deparmentId()).isEqualTo(25L);
        assertThat(response.department()).isEqualTo(department);
        assertThat(response.cityId()).isEqualTo(25001L);
        assertThat(response.city()).isEqualTo(city);
        assertThat(response.latitude()).isEqualTo(4.7016);
        assertThat(response.longitude()).isEqualTo(-74.1469);
    }

    @Test
    void shouldDeserializeJsonUsingExactExternalApiFieldNames()
            throws Exception {

        String json = """
                {
                    "id": 1,
                    "name": "Aeropuerto Internacional El Dorado",
                    "iataCode": "BOG",
                    "oaciCode": "SKBO",
                    "type": "Aeropuerto",
                    "deparmentId": 25,
                    "department": {
                        "id": 25,
                        "name": "Cundinamarca"
                    },
                    "cityId": 25001,
                    "city": {
                        "id": 25001,
                        "name": "Bogotá"
                    },
                    "latitude": 4.7016,
                    "longitude": -74.1469
                }
                """;

        ColombiaAirportResponse response =
                objectMapper.readValue(json, ColombiaAirportResponse.class);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name())
                .isEqualTo("Aeropuerto Internacional El Dorado");
        assertThat(response.iataCode()).isEqualTo("BOG");

        // Nombre EXACTO entregado por API Colombia
        assertThat(response.oaciCode()).isEqualTo("SKBO");

        assertThat(response.type()).isEqualTo("Aeropuerto");

        // Se verifica intencionalmente el typo de la API externa
        assertThat(response.deparmentId()).isEqualTo(25L);

        assertThat(response.department()).isNotNull();
        assertThat(response.city()).isNotNull();

        assertThat(response.cityId()).isEqualTo(25001L);
        assertThat(response.latitude()).isEqualTo(4.7016);
        assertThat(response.longitude()).isEqualTo(-74.1469);
    }

    @Test
    void shouldIgnoreUnknownJsonProperties() throws Exception {

        String json = """
                {
                    "id": 1,
                    "name": "Aeropuerto Internacional El Dorado",
                    "iataCode": "BOG",
                    "oaciCode": "SKBO",
                    "type": "Aeropuerto",
                    "deparmentId": 25,
                    "department": null,
                    "cityId": 25001,
                    "city": null,
                    "latitude": 4.7016,
                    "longitude": -74.1469,
                    "propertyAddedByFutureApiVersion": "unknown"
                }
                """;

        ColombiaAirportResponse response =
                objectMapper.readValue(json, ColombiaAirportResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.oaciCode()).isEqualTo("SKBO");
        assertThat(response.deparmentId()).isEqualTo(25L);
    }

    @Test
    void shouldAllowNullableOptionalFields() throws Exception {

        String json = """
                {
                    "id": 1,
                    "name": "Aeropuerto de prueba",
                    "iataCode": null,
                    "oaciCode": null,
                    "type": null,
                    "deparmentId": null,
                    "department": null,
                    "cityId": null,
                    "city": null,
                    "latitude": null,
                    "longitude": null
                }
                """;

        ColombiaAirportResponse response =
                objectMapper.readValue(json, ColombiaAirportResponse.class);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Aeropuerto de prueba");
        assertThat(response.iataCode()).isNull();
        assertThat(response.oaciCode()).isNull();
        assertThat(response.type()).isNull();
        assertThat(response.deparmentId()).isNull();
        assertThat(response.department()).isNull();
        assertThat(response.cityId()).isNull();
        assertThat(response.city()).isNull();
        assertThat(response.latitude()).isNull();
        assertThat(response.longitude()).isNull();
    }

    @Test
    void shouldSerializeRecordUsingExternalApiFieldNames()
            throws Exception {

        ColombiaAirportResponse response = new ColombiaAirportResponse(
                1L,
                "Aeropuerto Internacional El Dorado",
                "BOG",
                "SKBO",
                "Aeropuerto",
                25L,
                null,
                25001L,
                null,
                4.7016,
                -74.1469
        );

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"id\":1");
        assertThat(json)
                .contains("\"name\":\"Aeropuerto Internacional El Dorado\"");
        assertThat(json).contains("\"iataCode\":\"BOG\"");

        // Verifica el nombre externo correcto
        assertThat(json).contains("\"oaciCode\":\"SKBO\"");

        // Verifica el typo real de la API
        assertThat(json).contains("\"deparmentId\":25");

        assertThat(json).contains("\"cityId\":25001");
    }
}