package com.itinerarios.airport.resilience;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/**
 * Prueba de fallo controlado exigida por la sección 29 de la especificación
 * maestra: demuestra explícitamente la transición
 *
 *   CLOSED -> (fallos) -> OPEN -> (tiempo de espera) -> HALF_OPEN -> (éxito) -> CLOSED
 *
 * Usa la misma configuración (sliding window, umbral de fallos) que
 * `application.yml` para el circuito "colombiaApi", pero construida
 * directamente con la API de Resilience4j (sin contexto Spring), de modo
 * que la prueba sea determinística y no dependa de red real ni de mocks de
 * HTTP: se simulan los fallos invocando el circuito con una función que
 * lanza excepción a voluntad.
 */
class CircuitBreakerFailureTest {

    private CircuitBreaker buildCircuitBreakerLikeColombiaApi() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(200)) // acelerado para el test
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        return CircuitBreaker.of("colombiaApi-test", config);
    }

    @Test
    void circuitoTransicionaDeClosedAOpenTrasSuperarElUmbralDeFallos() {
        CircuitBreaker circuitBreaker = buildCircuitBreakerLikeColombiaApi();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        Supplier<String> failingCall = CircuitBreaker.decorateSupplier(circuitBreaker, this::alwaysFails);

        // minimumNumberOfCalls = 5, failureRateThreshold = 50%: con 5 fallos
        // consecutivos (100% de fallo) el circuito debe abrir.
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(failingCall::get).isInstanceOf(RuntimeException.class);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void circuitoAbiertoRechazaLlamadasSinEjecutarlas() {
        CircuitBreaker circuitBreaker = buildCircuitBreakerLikeColombiaApi();
        forceOpenState(circuitBreaker);

        Supplier<String> decorated = CircuitBreaker.decorateSupplier(circuitBreaker, this::alwaysSucceeds);

        // Con el circuito OPEN, ni siquiera se ejecuta la función real:
        // Resilience4j falla rápido con CallNotPermittedException.
        assertThatThrownBy(decorated::get).isInstanceOf(CallNotPermittedException.class);
    }

    @Test
    void circuitoTransicionaAHalfOpenYLuegoAClosedSiLasLlamadasDePruebaSonExitosas() {
        CircuitBreaker circuitBreaker = buildCircuitBreakerLikeColombiaApi();
        forceOpenState(circuitBreaker);

        // Esperar a que pase waitDurationInOpenState (200ms) para la
        // transición automática OPEN -> HALF_OPEN.
        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN));

        Supplier<String> succeedingCall = CircuitBreaker.decorateSupplier(circuitBreaker, this::alwaysSucceeds);

        // permittedNumberOfCallsInHalfOpenState = 3: tres llamadas de
        // prueba exitosas deben cerrar el circuito de nuevo.
        for (int i = 0; i < 3; i++) {
            assertThat(succeedingCall.get()).isEqualTo("ok");
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    private void forceOpenState(CircuitBreaker circuitBreaker) {
        Supplier<String> failingCall = CircuitBreaker.decorateSupplier(circuitBreaker, this::alwaysFails);
        for (int i = 0; i < 5; i++) {
            try {
                failingCall.get();
            } catch (RuntimeException ignored) {
                // esperado: forzando fallos para abrir el circuito
            }
        }
    }

    private String alwaysFails() {
        throw new RuntimeException("Fallo simulado de API Colombia");
    }

    private String alwaysSucceeds() {
        return "ok";
    }
}
