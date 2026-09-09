package com.itinerarios.airport.service;

import com.itinerarios.airport.adapter.ColombiaAirportAdapter;
import com.itinerarios.airport.dto.AirportDto;
import com.itinerarios.airport.dto.adapter.ColombiaAirportResponse;
import com.itinerarios.airport.entity.Airport;
import com.itinerarios.airport.exception.AirportNotFoundException;
import com.itinerarios.airport.mapper.ColombiaAirportMapper;
import com.itinerarios.airport.repository.AirportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Flujo de consulta por id (sección 22, sin Redis todavía - eso llega en Nivel 2):
 *
 *   PostgreSQL HIT  -> Response
 *   PostgreSQL MISS -> este método no aplica (id interno solo existe si ya se persistió)
 *
 * Flujo de consulta por código IATA (usado por Itinerary Service para validar):
 *
 *   PostgreSQL HIT  -> Response
 *   PostgreSQL MISS -> API Colombia -> Adapter -> persistir -> Response
 */
@Service
public class AirportService {

    private final AirportRepository airportRepository;
    private final ColombiaAirportAdapter colombiaAirportAdapter;
    private final ColombiaAirportMapper mapper;

    public AirportService(AirportRepository airportRepository,
                           ColombiaAirportAdapter colombiaAirportAdapter,
                           ColombiaAirportMapper mapper) {
        this.airportRepository = airportRepository;
        this.colombiaAirportAdapter = colombiaAirportAdapter;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AirportDto> findAll() {
        return airportRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AirportDto findById(Long id) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> AirportNotFoundException.byId(id));
        return mapper.toDto(airport);
    }

    /**
     * Punto de entrada usado por Itinerary Service para validar un aeropuerto
     * mediante HTTP (sección 14). Si no existe localmente, se consulta el
     * Adapter y se persiste antes de responder.
     */
    @Transactional
    public AirportDto findByIataCode(String iataCode) {
        return airportRepository.findByIataCodeIgnoreCase(iataCode)
                .map(mapper::toDto)
                .orElseGet(() -> fetchAndPersistFromExternal(iataCode));
    }

    private AirportDto fetchAndPersistFromExternal(String iataCode) {
        ColombiaAirportResponse external = colombiaAirportAdapter.findByIataCode(iataCode);
        if (external == null) {
            throw AirportNotFoundException.byIataCode(iataCode);
        }

        Airport airport = airportRepository.findByExternalId(String.valueOf(external.id()))
                .map(existing -> {
                    mapper.updateEntity(existing, external);
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(external));

        Airport saved = airportRepository.save(airport);
        return mapper.toDto(saved);
    }
}
