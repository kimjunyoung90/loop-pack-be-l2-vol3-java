package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MvProductRankMonthly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface MvProductRankMonthlyJpaRepository extends JpaRepository<MvProductRankMonthly, Long> {

    Page<MvProductRankMonthly> findAllByPeriodStartLessThanEqualAndPeriodEndGreaterThanEqualOrderByRankNumberAsc(
            LocalDate periodStart, LocalDate periodEnd, Pageable pageable);

    @Query("SELECT m FROM MvProductRankMonthly m " +
            "WHERE m.periodEnd = (SELECT MAX(m2.periodEnd) FROM MvProductRankMonthly m2 WHERE m2.periodEnd < :date) " +
            "ORDER BY m.rankNumber ASC")
    Page<MvProductRankMonthly> findLatestBefore(@Param("date") LocalDate date, Pageable pageable);
}
