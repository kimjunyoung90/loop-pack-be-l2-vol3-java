package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
public class RankingCacheRepositoryImpl implements RankingCacheRepository {

    private static final String KEY_PREFIX = "ranking:all:";

    private final RedisTemplate<String, String> redisTemplate;

    public RankingCacheRepositoryImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<Long> getTopRankedProductIds(String date, long offset, long size) {
        String key = KEY_PREFIX + date;
        try {
            Set<ZSetOperations.TypedTuple<String>> tuples =
                    redisTemplate.opsForZSet().reverseRangeWithScores(key, offset, offset + size - 1);
            if (tuples == null || tuples.isEmpty()) {
                return Collections.emptyList();
            }
            return tuples.stream()
                    .map(tuple -> Long.parseLong(tuple.getValue()))
                    .toList();
        } catch (Exception e) {
            log.warn("Redis 랭킹 조회 실패: key={}", key, e);
            return Collections.emptyList();
        }
    }

    @Override
    public long getTotalCount(String date) {
        String key = KEY_PREFIX + date;
        try {
            Long count = redisTemplate.opsForZSet().zCard(key);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.warn("Redis 랭킹 카운트 조회 실패: key={}", key, e);
            return 0;
        }
    }

    @Override
    public Long getRank(String date, Long productId) {
        String key = KEY_PREFIX + date;
        try {
            Long rank = redisTemplate.opsForZSet().reverseRank(key, String.valueOf(productId));
            return rank != null ? rank + 1 : null;
        } catch (Exception e) {
            log.warn("Redis 랭킹 순위 조회 실패: key={}, productId={}", key, productId, e);
            return null;
        }
    }
}
