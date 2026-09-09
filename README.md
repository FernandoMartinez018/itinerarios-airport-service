# itinerarios-airport-service

Microservicio del Airport Context — Sistema de Itinerarios Personales.

## Responsabilidad

- Integración con [API Colombia](https://api-colombia.com) mediante Adapter (`ColombiaAirportAdapter`).
- Persistencia local de aeropuertos en `airport_db` (PostgreSQL propio, sin FKs hacia otros microservicios).
- Exposición de una API REST consumida por `itinerarios-gateway` (nunca directamente por Angular).
- Endpoint `/api/airports/iata/{iataCode}` usado por `itinerarios-itinerary-service` para validar aeropuertos.

## Stack

- Java 21
- Spring Boot 4.1.1 (verificar compatibilidad con Spring Cloud Gateway antes de fijar versión definitiva — ver comentario en `pom.xml`)
- PostgreSQL + Flyway
- springdoc-openapi (Swagger UI en `/swagger-ui.html`)

## Ejecutar localmente

Requiere PostgreSQL disponible (ver `itinerarios-infrastructure/docker-compose.yml`).

```bash
export DB_HOST=localhost
export DB_PORT=5433
export DB_NAME=airport_db
export DB_USERNAME=airport_user
export DB_PASSWORD=changeme

mvn spring-boot:run
```

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/airports` | Lista aeropuertos ya sincronizados localmente |
| GET | `/api/airports/{id}` | Aeropuerto por id interno |
| GET | `/api/airports/iata/{iataCode}` | Aeropuerto por código IATA (consulta y sincroniza contra API Colombia si no existe localmente) |

## Pendiente (Nivel 2+)

- Cache Redis (patrón Cache-Aside) en el flujo de consulta.
- Retry / Backoff / Jitter / Circuit Breaker en `ColombiaAirportAdapter`.
- OpenTelemetry + Jaeger.
