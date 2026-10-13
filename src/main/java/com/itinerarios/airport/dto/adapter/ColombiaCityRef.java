package com.itinerarios.airport.dto.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ColombiaCityRef(Long id, String name) {
}
