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
                external.latitude() != null ? BigDecimal.valueOf(external.latitude()) : null,
                external.longitude() != null ? BigDecimal.valueOf(external.longitude()) : null
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
        airport.setLatitude(external.latitude() != null ? BigDecimal.valueOf(external.latitude()) : null);
        airport.setLongitude(external.longitude() != null ? BigDecimal.valueOf(external.longitude()) : null);
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

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
