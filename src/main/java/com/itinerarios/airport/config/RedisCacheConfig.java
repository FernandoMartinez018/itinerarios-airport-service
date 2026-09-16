package com.itinerarios.airport.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Cache-Aside con Redis (sección 27 de la especificación maestra). El flujo
 * real queda implementado por Spring vía @Cacheable sobre
 * AirportService.findByIataCode: Redis MISS -> ejecuta el método (que ya
 * contiene la lógica PostgreSQL -> API Colombia -> persistir) -> el
 * resultado se escribe en Redis automáticamente. Esto reproduce
 * exactamente el diagrama de flujo de la sección 22, sin necesidad de
 * escribir el manejo de cache a mano con RedisTemplate.
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    public static final String AIRPORTS_BY_IATA_CACHE = "airports-by-iata";
    public static final String AIRPORTS_BY_ID_CACHE = "airports-by-id";

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper,
            @Value("${cache.airports.ttl-minutes:60}") long ttlMinutes) {

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(ttlMinutes))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(Map.of(
                        AIRPORTS_BY_IATA_CACHE, defaultConfig,
                        AIRPORTS_BY_ID_CACHE, defaultConfig
                ))
                .build();
    }
}
