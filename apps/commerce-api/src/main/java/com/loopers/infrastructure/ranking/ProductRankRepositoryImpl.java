package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MvProductRankMonthly;
import com.loopers.domain.ranking.MvProductRankWeekly;
import com.loopers.domain.ranking.ProductRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@RequiredArgsConstructor
@Repository
public class ProductRankRepositoryImpl implements ProductRankRepository {

    private final MvProductRankWeeklyJpaRepository weeklyJpaRepository;
    private final MvProductRankMonthlyJpaRepository monthlyJpaRepository;

    @Override
    public Page<MvProductRankWeekly> findWeeklyRanks(LocalDate periodStart, LocalDate periodEnd, Pageable pageable) {
        return weeklyJpaRepository.findAllByPeriodStartAndPeriodEndOrderByRankNumberAsc(
                periodStart, periodEnd, pageable);
    }

    @Override
    public Page<MvProductRankMonthly> findMonthlyRanks(LocalDate periodStart, LocalDate periodEnd, Pageable pageable) {
        return monthlyJpaRepository.findAllByPeriodStartAndPeriodEndOrderByRankNumberAsc(
                periodStart, periodEnd, pageable);
    }
}
