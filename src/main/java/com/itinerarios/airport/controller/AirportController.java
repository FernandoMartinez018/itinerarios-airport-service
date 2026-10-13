package com.itinerarios.airport.controller;

import com.itinerarios.airport.dto.AirportDto;
import com.itinerarios.airport.service.AirportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
@Tag(name = "Airports", description = "Consulta de aeropuertos de Colombia")
public class AirportController {

    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @GetMapping
    @Operation(summary = "Lista los aeropuertos ya sincronizados en la base local")
    public ResponseEntity<List<AirportDto>> findAll() {
        return ResponseEntity.ok(airportService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un aeropuerto por su id interno")
    public ResponseEntity<AirportDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(airportService.findById(id));
    }

    @GetMapping("/iata/{iataCode}")
    @Operation(summary = "Obtiene (y sincroniza si es necesario) un aeropuerto por código IATA. "
            + "Usado por Itinerary Service para validar aeropuertos de un itinerario.")
    public ResponseEntity<AirportDto> findByIataCode(@PathVariable String iataCode) {
        return ResponseEntity.ok(airportService.findByIataCode(iataCode));
    }
}
