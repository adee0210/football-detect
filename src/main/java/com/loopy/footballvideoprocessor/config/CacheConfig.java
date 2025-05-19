package com.loopy.footballvideoprocessor.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class CacheConfig {

        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(60))
                                .disableCachingNullValues()
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(
                                                                new GenericJackson2JsonRedisSerializer(
                                                                                redisObjectMapper())));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(cacheConfig)
                                .withCacheConfiguration("videoCache",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMinutes(60))
                                                                .serializeValuesWith(
                                                                                RedisSerializationContext.SerializationPair
                                                                                                .fromSerializer(
                                                                                                                new GenericJackson2JsonRedisSerializer(
                                                                                                                                redisObjectMapper()))))
                                .withCacheConfiguration("videoListCache",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMinutes(15))
                                                                .serializeValuesWith(
                                                                                RedisSerializationContext.SerializationPair
                                                                                                .fromSerializer(
                                                                                                                new GenericJackson2JsonRedisSerializer(
                                                                                                                                redisObjectMapper()))))
                                .withCacheConfiguration("users",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMinutes(5))
                                                                .serializeValuesWith(
                                                                                RedisSerializationContext.SerializationPair
                                                                                                .fromSerializer(
                                                                                                                new GenericJackson2JsonRedisSerializer(
                                                                                                                                redisObjectMapper()))))
                                .withCacheConfiguration("dashboard",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMinutes(1))
                                                                .serializeValuesWith(
                                                                                RedisSerializationContext.SerializationPair
                                                                                                .fromSerializer(
                                                                                                                new GenericJackson2JsonRedisSerializer(
                                                                                                                                redisObjectMapper()))))
                                .build();
        }

        private ObjectMapper redisObjectMapper() {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                mapper.activateDefaultTyping(
                                LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                return mapper;
        }
}
