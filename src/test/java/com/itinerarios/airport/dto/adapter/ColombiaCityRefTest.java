package com.itinerarios.airport.dto.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itinerarios.airport.dto.adapter.ColombiaCityRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColombiaCityRefTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateColombiaCityRefWithCorrectValues() {

        ColombiaCityRef city = new ColombiaCityRef(
                25001L,
                "Bogotá"
        );

        assertThat(city.id()).isEqualTo(25001L);
        assertThat(city.name()).isEqualTo("Bogotá");
    }

    @Test
    void shouldDeserializeJsonCorrectly() throws Exception {

        String json = """
                {
                    "id": 25001,
                    "name": "Bogotá"
                }
                """;

        ColombiaCityRef city =
                objectMapper.readValue(json, ColombiaCityRef.class);

        assertThat(city.id()).isEqualTo(25001L);
        assertThat(city.name()).isEqualTo("Bogotá");
    }

    @Test
    void shouldIgnoreUnknownJsonProperties() throws Exception {

        String json = """
                {
                    "id": 25001,
                    "name": "Bogotá",
                    "unknownProperty": "valor desconocido"
                }
                """;

        ColombiaCityRef city =
                objectMapper.readValue(json, ColombiaCityRef.class);

        assertThat(city).isNotNull();
        assertThat(city.id()).isEqualTo(25001L);
        assertThat(city.name()).isEqualTo("Bogotá");
    }

    @Test
    void shouldAllowNullValues() throws Exception {

        String json = """
                {
                    "id": null,
                    "name": null
                }
                """;

        ColombiaCityRef city =
                objectMapper.readValue(json, ColombiaCityRef.class);

        assertThat(city.id()).isNull();
        assertThat(city.name()).isNull();
    }

    @Test
    void shouldSerializeCorrectly() throws Exception {

        ColombiaCityRef city = new ColombiaCityRef(
                25001L,
                "Bogotá"
        );

        String json = objectMapper.writeValueAsString(city);

        assertThat(json).contains("\"id\":25001");
        assertThat(json).contains("\"name\":\"Bogotá\"");
    }
}