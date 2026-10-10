package com.itinerarios.airport.mapper;

import com.itinerarios.airport.dto.AirportDto;
import com.itinerarios.airport.dto.adapter.ColombiaAirportResponse;
import com.itinerarios.airport.entity.Airport;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ColombiaAirportMapper {

    /**
     * Traduce el modelo externo de API Colombia al agregado interno Airport.
     * Aquí ocurre el renombrado deliberado: oaciCode -> icaoCode.
     */
    public Airport toEntity(ColombiaAirportResponse external) {
        String city = external.city() != null ? external.city().name() : null;
        String department = external.department() != null ? external.department().name() : null;

        Airport airport = new Airport(
                String.valueOf(external.id()),
                blankToNull(external.iataCode()),
                blankToNull(external.oaciCode()),
                external.name(),
                city,
                department,
                latitude(external),
                longitude(external)
        );
        airport.setActive(true);
        return airport;
    }

    /** Actualiza una entidad ya persistida con datos frescos del proveedor externo. */
    public void updateEntity(Airport airport, ColombiaAirportResponse external) {
        airport.setIataCode(blankToNull(external.iataCode()));
        airport.setIcaoCode(blankToNull(external.oaciCode()));
        airport.setName(external.name());
        airport.setCity(external.city() != null ? external.city().name() : null);
        airport.setDepartment(external.department() != null ? external.department().name() : null);
        airport.setLatitude(latitude(external));
        airport.setLongitude(longitude(external));
    }

    // API Colombia invierte latitude/longitude en casi todos sus aeropuertos (51 de 52, 2026-10-09).
    // En Colombia |lat| <= ~14 y |lon| >= ~66: una "latitud" > 20 con "longitud" <= 20 esta invertida.
    private static boolean swapped(ColombiaAirportResponse external) {
        return external.latitude() != null && external.longitude() != null
                && Math.abs(external.latitude()) > 20 && Math.abs(external.longitude()) <= 20;
    }

    private static BigDecimal latitude(ColombiaAirportResponse external) {
        Double value = swapped(external) ? external.longitude() : external.latitude();
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    private static BigDecimal longitude(ColombiaAirportResponse external) {
        Double value = swapped(external) ? external.latitude() : external.longitude();
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    public AirportDto toDto(Airport airport) {
        return new AirportDto(
                airport.getId(),
                airport.getExternalId(),
                airport.getIataCode(),
                airport.getIcaoCode(),
                airport.getName(),
                airport.getCity(),
                airport.getDepartment(),
                airport.getLatitude() != null ? airport.getLatitude().doubleValue() : null,
                airport.getLongitude() != null ? airport.getLongitude().doubleValue() : null,
                airport.isActive()
        );
    }

    // API Colombia usa "N/A" como codigo ausente; guardarlo literal romperia los UNIQUE de iata/icao.
    private String blankToNull(String value) {
        return (value == null || value.isBlank() || "N/A".equalsIgnoreCase(value.trim())) ? null : value;
    }
}
