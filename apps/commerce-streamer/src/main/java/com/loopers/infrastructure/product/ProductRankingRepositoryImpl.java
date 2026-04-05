package com.loopers.infrastructure.product;

import com.loopers.config.redis.RedisConfig;
import com.loopers.domain.product.ProductRankingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Repository
public class ProductRankingRepositoryImpl implements ProductRankingRepository {

    private static final String KEY_PREFIX = "ranking:all:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Duration TTL = Duration.ofDays(2);

    private final RedisTemplate<String, String> redisTemplate;

    public ProductRankingRepositoryImpl(
            @Qualifier(RedisConfig.REDIS_TEMPLATE_MASTER) RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void incrementScores(Map<Long, Double> productScores) {
        if (productScores.isEmpty()) {
            return;
        }

        String key = KEY_PREFIX + LocalDate.now().format(DATE_FORMATTER);

        try {
            redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                @SuppressWarnings("unchecked")
                public Object execute(RedisOperations operations) throws DataAccessException {
                    for (Map.Entry<Long, Double> entry : productScores.entrySet()) {
                        operations.opsForZSet().incrementScore(
                                key,
                                String.valueOf(entry.getKey()),
                                entry.getValue()
                        );
                    }
                    operations.expire(key, TTL);
                    return null;
                }
            });
        } catch (Exception e) {
            log.warn("Redis 랭킹 점수 갱신 실패: key={}", key, e);
        }
    }
}
