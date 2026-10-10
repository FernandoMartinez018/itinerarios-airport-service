package com.itinerarios.airport.repository;

import com.itinerarios.airport.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AirportRepository extends JpaRepository<Airport, Long> {

    Optional<Airport> findByExternalId(String externalId);

    Optional<Airport> findByIataCodeIgnoreCase(String iataCode);

    boolean existsByIataCodeIgnoreCase(String iataCode);
}
