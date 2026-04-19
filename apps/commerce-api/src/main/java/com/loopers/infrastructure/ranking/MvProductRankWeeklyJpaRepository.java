package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MvProductRankWeekly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface MvProductRankWeeklyJpaRepository extends JpaRepository<MvProductRankWeekly, Long> {

    Page<MvProductRankWeekly> findAllByPeriodStartLessThanEqualAndPeriodEndGreaterThanEqualOrderByRankNumberAsc(
            LocalDate periodStart, LocalDate periodEnd, Pageable pageable);

    @Query("SELECT w FROM MvProductRankWeekly w " +
            "WHERE w.periodEnd = (SELECT MAX(w2.periodEnd) FROM MvProductRankWeekly w2 WHERE w2.periodEnd < :date) " +
            "ORDER BY w.rankNumber ASC")
    Page<MvProductRankWeekly> findLatestBefore(@Param("date") LocalDate date, Pageable pageable);
}
