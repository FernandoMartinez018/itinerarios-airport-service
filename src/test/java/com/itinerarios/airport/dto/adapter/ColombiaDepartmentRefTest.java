package com.itinerarios.airport.dto.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColombiaDepartmentRefTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateColombiaDepartmentRefWithCorrectValues() {

        ColombiaDepartmentRef department = new ColombiaDepartmentRef(
                25L,
                "Cundinamarca"
        );

        assertThat(department.id()).isEqualTo(25L);
        assertThat(department.name()).isEqualTo("Cundinamarca");
    }

    @Test
    void shouldDeserializeJsonCorrectly() throws Exception {

        String json = """
                {
                    "id": 25,
                    "name": "Cundinamarca"
                }
                """;

        ColombiaDepartmentRef department =
                objectMapper.readValue(json, ColombiaDepartmentRef.class);

        assertThat(department.id()).isEqualTo(25L);
        assertThat(department.name()).isEqualTo("Cundinamarca");
    }

    @Test
    void shouldIgnoreUnknownJsonProperties() throws Exception {

        String json = """
                {
                    "id": 25,
                    "name": "Cundinamarca",
                    "unknownProperty": "valor desconocido"
                }
                """;

        ColombiaDepartmentRef department =
                objectMapper.readValue(json, ColombiaDepartmentRef.class);

        assertThat(department).isNotNull();
        assertThat(department.id()).isEqualTo(25L);
        assertThat(department.name()).isEqualTo("Cundinamarca");
    }

    @Test
    void shouldAllowNullValues() throws Exception {

        String json = """
                {
                    "id": null,
                    "name": null
                }
                """;

        ColombiaDepartmentRef department =
                objectMapper.readValue(json, ColombiaDepartmentRef.class);

        assertThat(department.id()).isNull();
        assertThat(department.name()).isNull();
    }

    @Test
    void shouldSerializeCorrectly() throws Exception {

        ColombiaDepartmentRef department = new ColombiaDepartmentRef(
                25L,
                "Cundinamarca"
        );

        String json = objectMapper.writeValueAsString(department);

        assertThat(json).contains("\"id\":25");
        assertThat(json).contains("\"name\":\"Cundinamarca\"");
    }
}