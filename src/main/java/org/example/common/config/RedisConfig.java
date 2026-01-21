package org.example.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@EnableCaching
@Configuration
public class RedisConfig {

        /**
         * ✅ LocalDate, LocalDateTime 직렬화 가능 ObjectMapper
         */
        @Bean
        public ObjectMapper redisObjectMapper() {
                return JsonMapper.builder()
                                .addModule(new JavaTimeModule())
                                .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                                                false)
                                .build();
        }

        /**
         * ✅ RedisTemplate (DTO 캐시용)
         */
        @Bean
        public RedisTemplate<String, Object> redisTemplate(
                        RedisConnectionFactory connectionFactory,
                        ObjectMapper redisObjectMapper) {
                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // key: String
                template.setKeySerializer(new StringRedisSerializer());

                // value: JSON (LocalDate 포함)
                template.setValueSerializer(
                                new GenericJackson2JsonRedisSerializer(redisObjectMapper));

                // hash key / value (혹시 모를 사용 대비)
                template.setHashKeySerializer(new StringRedisSerializer());
                template.setHashValueSerializer(
                                new GenericJackson2JsonRedisSerializer(redisObjectMapper));

                template.afterPropertiesSet();
                return template;
        }

        /**
         * ✅ RedisCacheManager 설정 (Spring Cache용)
         */
        @Bean
        public RedisCacheManager cacheManager(
                        RedisConnectionFactory connectionFactory,
                        ObjectMapper redisObjectMapper) {
                RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                                new GenericJackson2JsonRedisSerializer(redisObjectMapper)))
                                .entryTtl(Duration.ofHours(1)); // 1시간 캐시

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(config)
                                .build();
        }
}
