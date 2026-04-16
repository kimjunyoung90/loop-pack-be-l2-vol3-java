package com.loopers.infrastructure.ranking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.ranking.result.CachedProductRanks;
import com.loopers.domain.ranking.RankingPeriodCacheRepository;
import com.loopers.infrastructure.cache.RedisCacheRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class RankingPeriodCacheRepositoryImpl extends RedisCacheRepository implements RankingPeriodCacheRepository {

    private static final String WEEKLY_KEY_PREFIX = "ranking:weekly:";
    private static final String MONTHLY_KEY_PREFIX = "ranking:monthly:";
    private static final Duration TTL = Duration.ofHours(1);

    public RankingPeriodCacheRepositoryImpl(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    @Override
    public Optional<CachedProductRanks> getWeeklyRanks(LocalDate periodStart, Pageable pageable) {
        String key = WEEKLY_KEY_PREFIX + periodStart + ":" + buildPageSuffix(pageable);
        return getFromCache(key, CachedProductRanks.class);
    }

    @Override
    public void putWeeklyRanks(LocalDate periodStart, Pageable pageable, CachedProductRanks ranks) {
        String key = WEEKLY_KEY_PREFIX + periodStart + ":" + buildPageSuffix(pageable);
        putToCache(key, ranks, TTL);
    }

    @Override
    public Optional<CachedProductRanks> getMonthlyRanks(LocalDate periodStart, Pageable pageable) {
        String key = MONTHLY_KEY_PREFIX + periodStart + ":" + buildPageSuffix(pageable);
        return getFromCache(key, CachedProductRanks.class);
    }

    @Override
    public void putMonthlyRanks(LocalDate periodStart, Pageable pageable, CachedProductRanks ranks) {
        String key = MONTHLY_KEY_PREFIX + periodStart + ":" + buildPageSuffix(pageable);
        putToCache(key, ranks, TTL);
    }

    private String buildPageSuffix(Pageable pageable) {
        String sort = pageable.getSort().stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .collect(Collectors.joining("_"));
        if (sort.isEmpty()) {
            sort = "unsorted";
        }
        return "page:" + pageable.getPageNumber() + ":size:" + pageable.getPageSize() + ":sort:" + sort;
    }
}
