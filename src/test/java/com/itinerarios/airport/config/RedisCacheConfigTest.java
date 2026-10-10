package com.itinerarios.airport.config;

import com.itinerarios.airport.dto.AirportDto;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import tools.jackson.databind.json.JsonMapper;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Un HIT de cache debe devolver AirportDto, no LinkedHashMap (ClassCastException en el Service). */
class RedisCacheConfigTest {

    @Test
    void cachesDeAeropuertos_hacenRoundTripComoAirportDto() {
        var manager = new RedisCacheConfig().cacheManager(mock(RedisConnectionFactory.class), JsonMapper.builder().build(), 60);
        AirportDto original = new AirportDto(1L, "1", "BOG", "SKBO", "El Dorado", "Bogota", "Cundinamarca",
                4.7016, -74.1469, true);

        for (String cache : new String[]{RedisCacheConfig.AIRPORTS_BY_IATA_CACHE, RedisCacheConfig.AIRPORTS_BY_ID_CACHE}) {
            RedisCacheConfiguration config = ((RedisCache) manager.getCache(cache)).getCacheConfiguration();
            ByteBuffer written = config.getValueSerializationPair().write(original);
            assertThat(config.getValueSerializationPair().read(written)).isEqualTo(original);
        }
    }
}
