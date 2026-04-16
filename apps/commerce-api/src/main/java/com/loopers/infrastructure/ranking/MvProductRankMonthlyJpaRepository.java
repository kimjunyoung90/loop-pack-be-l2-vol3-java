package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MvProductRankMonthly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface MvProductRankMonthlyJpaRepository extends JpaRepository<MvProductRankMonthly, Long> {

    Page<MvProductRankMonthly> findAllByPeriodStartAndPeriodEndOrderByRankNumberAsc(
            LocalDate periodStart, LocalDate periodEnd, Pageable pageable);
}
