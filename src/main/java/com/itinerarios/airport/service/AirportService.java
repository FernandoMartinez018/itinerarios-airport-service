package com.itinerarios.airport.service;

import com.itinerarios.airport.adapter.ColombiaAirportAdapter;
import com.itinerarios.airport.config.RedisCacheConfig;
import com.itinerarios.airport.dto.AirportDto;
import com.itinerarios.airport.dto.adapter.ColombiaAirportResponse;
import com.itinerarios.airport.entity.Airport;
import com.itinerarios.airport.exception.AirportNotFoundException;
import com.itinerarios.airport.mapper.ColombiaAirportMapper;
import com.itinerarios.airport.repository.AirportRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Flujo de consulta por id (Redis cache-aside, sección 27):
 *
 *   Redis HIT  -> Response
 *   Redis MISS -> PostgreSQL HIT/MISS -> Response -> se escribe en Redis
 *
 * Flujo de consulta por código IATA (usado por Itinerary Service para validar,
 * sección 22 -- este es el que reproduce el diagrama completo de la
 * especificación maestra):
 *
 *   Redis HIT  -> Response
 *   Redis MISS -> PostgreSQL HIT -> Response -> se escribe en Redis
 *              -> PostgreSQL MISS -> API Colombia -> Adapter -> persistir -> Response -> se escribe en Redis
 *
 * Los métodos anotados con @Cacheable delegan el patrón Cache-Aside en
 * Spring: en un MISS, Spring ejecuta el cuerpo del método (que ya contiene
 * toda la cadena PostgreSQL -> API Colombia) y cachea automáticamente el
 * resultado devuelto -- no hace falta manejar RedisTemplate a mano. Un
 * resultado de error (excepción lanzada) nunca se cachea, intencionalmente:
 * no queremos que un 404 quede "pegado" en cache si el aeropuerto aparece
 * después en API Colombia.
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

    @Cacheable(cacheNames = RedisCacheConfig.AIRPORTS_BY_ID_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public AirportDto findById(Long id) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> AirportNotFoundException.byId(id));
        return mapper.toDto(airport);
    }

    /**
     * Punto de entrada usado por Itinerary Service para validar un aeropuerto
     * mediante HTTP (sección 14). Si no existe localmente, se consulta el
     * Adapter y se persiste antes de responder. El resultado completo queda
     * cacheado en Redis bajo la clave del código IATA (normalizado a
     * mayúsculas para que "bog" y "BOG" compartan la misma entrada de cache).
     */
    @Cacheable(cacheNames = RedisCacheConfig.AIRPORTS_BY_IATA_CACHE, key = "#iataCode.toUpperCase()")
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
