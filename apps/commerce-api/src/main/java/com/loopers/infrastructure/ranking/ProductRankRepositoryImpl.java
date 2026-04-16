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
    public Page<MvProductRankWeekly> findWeeklyRanks(LocalDate date, Pageable pageable) {
        return weeklyJpaRepository.findAllByPeriodStartLessThanEqualAndPeriodEndGreaterThanEqualOrderByRankNumberAsc(
                date, date, pageable);
    }

    @Override
    public Page<MvProductRankMonthly> findMonthlyRanks(LocalDate date, Pageable pageable) {
        return monthlyJpaRepository.findAllByPeriodStartLessThanEqualAndPeriodEndGreaterThanEqualOrderByRankNumberAsc(
                date, date, pageable);
    }
}
