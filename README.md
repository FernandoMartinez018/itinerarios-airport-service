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

## Resiliencia (Nivel 2, Fase 10)

`ColombiaAirportAdapter.findAll()` y `findByKeyword()` están protegidos con
`@Retry` + `@CircuitBreaker` (Resilience4j), configurados en `application.yml`:

- **Retry**: hasta 3 intentos, backoff exponencial con jitter (multiplicador 2x,
  hasta 50% de variación aleatoria) — evita que múltiples instancias reintenten
  exactamente al mismo tiempo.
- **Circuit Breaker**: ventana de 10 llamadas, mínimo 5 para evaluar, abre con
  ≥50% de fallos, permanece abierto 15s, permite 3 llamadas de prueba en
  half-open.

Ambas operaciones son GET (idempotentes), por lo que reintentarlas es seguro.
Cuando el circuito está abierto, el fallback lanza `ExternalProviderException`
(HTTP 502) en vez de intentar la llamada real — falla rápido en vez de esperar
un timeout en cada request mientras el proveedor está caído.

Ver `CircuitBreakerFailureTest` para la prueba de fallo controlado que demuestra
la transición `CLOSED -> OPEN -> HALF_OPEN -> CLOSED` (sección 29).

## Cache (Nivel 2, Fase 9)

`findById` y `findByIataCode` están anotados con `@Cacheable` (Redis, patrón
Cache-Aside, sección 27). En un MISS, Spring ejecuta el método completo —incluida
la cadena PostgreSQL → API Colombia cuando aplica— y cachea automáticamente el
resultado devuelto. Los errores (excepciones) nunca se cachean, para que un 404
no quede "pegado" si el aeropuerto aparece más tarde en API Colombia.

TTL configurable vía `AIRPORTS_CACHE_TTL_MINUTES` (default 60 minutos).

Para demostrar el hit/miss (evidencia de la sección 96): llamar dos veces seguidas
a `GET /api/airports/iata/{codigo}` y comparar el tiempo de respuesta, o inspeccionar
las claves en Redis con `redis-cli KEYS "airports-by-iata::*"`.

## Observabilidad (Nivel 2, Fase 11)

Trazas distribuidas vía **auto-instrumentación** con el agente Java de OpenTelemetry
(sin cambios de código): el Dockerfile descarga `opentelemetry-javaagent.jar` y
`docker-compose.yml` lo activa con `JAVA_TOOL_OPTIONS=-javaagent:...`. Las trazas
salen por OTLP/gRPC hacia `otel-collector`, que las reenvía a Jaeger
(`http://localhost:16686`).

Métricas vía Micrometer + `micrometer-registry-prometheus`, expuestas en
`/actuator/prometheus` y scrapeadas por Prometheus (`http://localhost:9090`),
visualizables en Grafana (`http://localhost:3000`, datasource ya provisionado).

**Conviven dos identificadores distintos y complementarios en los logs**: el
`correlationId` propio (`X-Correlation-ID`, sección 63, ya implementado desde
Nivel 1) para rastrear un request de negocio de punta a punta en los logs de
texto, y el `trace_id`/`span_id` que el agente de OpenTelemetry agrega
automáticamente al MDC para correlacionar con la traza visual en Jaeger. No se
unificaron deliberadamente — resolver ambos con el mismo mecanismo hubiera
requerido reemplazar el filtro de correlación ya probado de Nivel 1 sin
necesidad real.

## Pendiente (Nivel 2+, restante)

Ninguno — Nivel 2 completo para este servicio. Lo que sigue es Nivel 3.
