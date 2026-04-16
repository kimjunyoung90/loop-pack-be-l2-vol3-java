package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MvProductRankMonthly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface MvProductRankMonthlyJpaRepository extends JpaRepository<MvProductRankMonthly, Long> {

    Page<MvProductRankMonthly> findAllByPeriodStartAndPeriodEndOrderByRankNumberAsc(
            LocalDate periodStart, LocalDate periodEnd, Pageable pageable);

    @Query("SELECT MAX(r.periodStart) FROM MvProductRankMonthly r")
    LocalDate findLatestPeriodStart();
}
