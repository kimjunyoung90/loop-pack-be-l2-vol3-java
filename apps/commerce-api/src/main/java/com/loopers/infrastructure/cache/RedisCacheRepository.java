package com.loopers.infrastructure.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Slf4j
public abstract class RedisCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper cacheObjectMapper;
    private final Duration ttl;

    protected RedisCacheRepository(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.cacheObjectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.cacheObjectMapper.addMixIn(BaseEntity.class, EntityCacheMixin.class);
        this.ttl = ttl;
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class EntityCacheMixin {}

    // === 캐시 조회/저장 ===

    protected <T> Optional<T> getFromCache(String key, Class<T> type) {
        String json = safeGet(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(cacheObjectMapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            log.warn("캐시 역직렬화 실패: key={}", key, e);
            safeDelete(key);
            return Optional.empty();
        }
    }

    protected void putToCache(String key, Object value) {
        try {
            String json = cacheObjectMapper.writeValueAsString(value);
            safeSet(key, json);
        } catch (JsonProcessingException e) {
            log.warn("캐시 직렬화 실패: key={}", key, e);
        }
    }

    // === Redis 안전 연산 (Fail-Silent) ===

    protected void safeDelete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis 삭제 실패: key={}", key, e);
        }
    }

    protected void safeDeleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis 패턴 삭제 실패: pattern={}", pattern, e);
        }
    }

    private String safeGet(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis 조회 실패: key={}", key, e);
            return null;
        }
    }

    private void safeSet(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("Redis 저장 실패: key={}", key, e);
        }
    }
}
