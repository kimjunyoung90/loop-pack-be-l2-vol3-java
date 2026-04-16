package com.loopers.application.ranking;

import com.loopers.application.ranking.result.CachedProductRanks;
import com.loopers.application.ranking.result.ProductRankResult;
import com.loopers.domain.ranking.ProductRankRepository;
import com.loopers.domain.ranking.RankingCacheRepository;
import com.loopers.domain.ranking.RankingPeriodCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RankingService {

    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter REDIS_KEY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RankingCacheRepository rankingCacheRepository;
    private final RankingPeriodCacheRepository rankingPeriodCacheRepository;
    private final ProductRankRepository productRankRepository;

    @Transactional(readOnly = true)
    public List<Long> getRankedProductIds(String date, long offset, int size) {
        String redisKey = resolveDailyRedisKey(date);
        return rankingCacheRepository.getTopRankedProductIds(redisKey, offset, size);
    }

    @Transactional(readOnly = true)
    public long getTotalCount(String date) {
        String redisKey = resolveDailyRedisKey(date);
        return rankingCacheRepository.getTotalCount(redisKey);
    }

    public Long getRank(Long productId) {
        String today = LocalDate.now().format(REDIS_KEY_DATE_FORMATTER);
        return rankingCacheRepository.getRank(today, productId);
    }

    @Transactional(readOnly = true)
    public Page<ProductRankResult> getWeeklyRanks(String date, Pageable pageable) {
        LocalDate baseDate = resolveBaseDate(date);
        LocalDate periodStart = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Optional<CachedProductRanks> cached = rankingPeriodCacheRepository.getWeeklyRanks(periodStart, pageable);
        if (cached.isPresent()) {
            return toPage(cached.get(), pageable);
        }

        Page<ProductRankResult> result = productRankRepository.findWeeklyRanks(baseDate, pageable)
                .map(ProductRankResult::from);

        if (result.isEmpty()) {
            result = productRankRepository.findLatestWeeklyRanks(baseDate, pageable)
                    .map(ProductRankResult::from);
        }

        if (!result.isEmpty()) {
            rankingPeriodCacheRepository.putWeeklyRanks(periodStart, pageable,
                    new CachedProductRanks(result.getContent(), result.getTotalElements()));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Page<ProductRankResult> getMonthlyRanks(String date, Pageable pageable) {
        LocalDate baseDate = resolveBaseDate(date);
        LocalDate periodStart = baseDate.withDayOfMonth(1);

        Optional<CachedProductRanks> cached = rankingPeriodCacheRepository.getMonthlyRanks(periodStart, pageable);
        if (cached.isPresent()) {
            return toPage(cached.get(), pageable);
        }

        Page<ProductRankResult> result = productRankRepository.findMonthlyRanks(baseDate, pageable)
                .map(ProductRankResult::from);

        if (result.isEmpty()) {
            result = productRankRepository.findLatestMonthlyRanks(baseDate, pageable)
                    .map(ProductRankResult::from);
        }

        if (!result.isEmpty()) {
            rankingPeriodCacheRepository.putMonthlyRanks(periodStart, pageable,
                    new CachedProductRanks(result.getContent(), result.getTotalElements()));
        }

        return result;
    }

    private Page<ProductRankResult> toPage(CachedProductRanks cached, Pageable pageable) {
        return new PageImpl<>(cached.ranks(), pageable, cached.totalElements());
    }

    private String resolveDailyRedisKey(String date) {
        LocalDate baseDate = resolveBaseDate(date);
        return baseDate.format(REDIS_KEY_DATE_FORMATTER);
    }

    private LocalDate resolveBaseDate(String date) {
        return (date != null) ? LocalDate.parse(date, API_DATE_FORMATTER) : LocalDate.now();
    }
}
