package com.itinerarios.airport.dto.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ColombiaDepartmentRef(Long id, String name) {
}
