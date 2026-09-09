package com.itinerarios.airport.dto.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Refleja EXACTAMENTE el contrato de GET /api/v1/Airport de API Colombia,
 * confirmado en https://docs.api-colombia.com/operations/get-api-v1-Airport.html
 *
 * OJO: "oaciCode" (no "icaoCode") y "deparmentId" (typo real de la API externa,
 * falta la "t") son nombres tal cual los entrega el proveedor. No "corregir"
 * aquí; el renombrado correcto ocurre en el Mapper hacia el modelo interno.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ColombiaAirportResponse(
        Long id,
        String name,
        String iataCode,
        String oaciCode,
        String type,
        Long deparmentId,
        ColombiaDepartmentRef department,
        Long cityId,
        ColombiaCityRef city,
        Double latitude,
        Double longitude
) {
}
