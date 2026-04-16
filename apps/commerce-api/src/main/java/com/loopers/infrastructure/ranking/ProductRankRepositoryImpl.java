package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MvProductRankMonthly;
import com.loopers.domain.ranking.MvProductRankWeekly;
import com.loopers.domain.ranking.ProductRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@RequiredArgsConstructor
@Repository
public class ProductRankRepositoryImpl implements ProductRankRepository {

    private final MvProductRankWeeklyJpaRepository weeklyJpaRepository;
    private final MvProductRankMonthlyJpaRepository monthlyJpaRepository;

    @Override
    public Page<MvProductRankWeekly> findLatestWeeklyRanks(Pageable pageable) {
        LocalDate latestPeriodStart = weeklyJpaRepository.findLatestPeriodStart();
        if (latestPeriodStart == null) {
            return Page.empty(pageable);
        }
        LocalDate periodEnd = latestPeriodStart.plusDays(6);
        return weeklyJpaRepository.findAllByPeriodStartAndPeriodEndOrderByRankNumberAsc(
                latestPeriodStart, periodEnd, pageable);
    }

    @Override
    public Page<MvProductRankMonthly> findLatestMonthlyRanks(Pageable pageable) {
        LocalDate latestPeriodStart = monthlyJpaRepository.findLatestPeriodStart();
        if (latestPeriodStart == null) {
            return Page.empty(pageable);
        }
        LocalDate periodEnd = latestPeriodStart.with(TemporalAdjusters.lastDayOfMonth());
        return monthlyJpaRepository.findAllByPeriodStartAndPeriodEndOrderByRankNumberAsc(
                latestPeriodStart, periodEnd, pageable);
    }
}
