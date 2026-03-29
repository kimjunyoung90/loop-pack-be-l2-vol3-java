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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public abstract class RedisCacheRepository {

    private static final double JITTER_RATE = 0.1;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper cacheObjectMapper;

    protected RedisCacheRepository(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.cacheObjectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.cacheObjectMapper.addMixIn(BaseEntity.class, EntityCacheMixin.class);
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

    protected void putToCache(String key, Object value, Duration ttl) {
        try {
            String json = cacheObjectMapper.writeValueAsString(value);
            safeSet(key, json, applyJitter(ttl));
        } catch (JsonProcessingException e) {
            log.warn("캐시 직렬화 실패: key={}", key, e);
        }
    }

    private Duration applyJitter(Duration baseTtl) {
        long baseSeconds = baseTtl.getSeconds();
        long jitter = (long) (baseSeconds * JITTER_RATE * (ThreadLocalRandom.current().nextDouble() * 2 - 1));
        return Duration.ofSeconds(Math.max(1, baseSeconds + jitter));
    }

    protected <T> List<T> multiGetFromCache(List<String> keys, Class<T> type) {
        if (keys.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> jsonList = safeMultiGet(keys);
        if (jsonList == null) {
            return Collections.emptyList();
        }
        return jsonList.stream()
                .filter(Objects::nonNull)
                .map(json -> {
                    try {
                        return cacheObjectMapper.readValue(json, type);
                    } catch (JsonProcessingException e) {
                        log.warn("캐시 역직렬화 실패", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
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

    private List<String> safeMultiGet(List<String> keys) {
        try {
            return redisTemplate.opsForValue().multiGet(keys);
        } catch (Exception e) {
            log.warn("Redis 다건 조회 실패: keys={}", keys.size(), e);
            return null;
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

    private void safeSet(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("Redis 저장 실패: key={}", key, e);
        }
    }
}
