package com.itinerarios.airport.exception;

public class AirportNotFoundException extends RuntimeException {

    public AirportNotFoundException(String message) {
        super(message);
    }

    public static AirportNotFoundException byId(Long id) {
        return new AirportNotFoundException("Airport not found with id: " + id);
    }

    public static AirportNotFoundException byIataCode(String iataCode) {
        return new AirportNotFoundException("Airport not found with IATA code: " + iataCode);
    }
}
