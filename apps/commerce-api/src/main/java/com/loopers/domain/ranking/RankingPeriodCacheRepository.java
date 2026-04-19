package com.loopers.domain.ranking;

import com.loopers.application.ranking.result.CachedProductRanks;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface RankingPeriodCacheRepository {

    Optional<CachedProductRanks> getWeeklyRanks(LocalDate periodStart, Pageable pageable);

    void putWeeklyRanks(LocalDate periodStart, Pageable pageable, CachedProductRanks ranks);

    Optional<CachedProductRanks> getMonthlyRanks(LocalDate periodStart, Pageable pageable);

    void putMonthlyRanks(LocalDate periodStart, Pageable pageable, CachedProductRanks ranks);
}
