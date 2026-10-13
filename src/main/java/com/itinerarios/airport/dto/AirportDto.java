package com.itinerarios.airport.dto;

/**
 * Representación de salida HTTP de un aeropuerto.
 * No expone directamente la entidad JPA (sección 20 de la especificación maestra).
 */
public record AirportDto(
        Long id,
        String externalId,
        String iataCode,
        String icaoCode,
        String name,
        String city,
        String department,
        Double latitude,
        Double longitude,
        boolean active
) {
}
