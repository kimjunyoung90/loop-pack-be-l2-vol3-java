package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MvProductRankWeekly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface MvProductRankWeeklyJpaRepository extends JpaRepository<MvProductRankWeekly, Long> {

    Page<MvProductRankWeekly> findAllByPeriodStartLessThanEqualAndPeriodEndGreaterThanEqualOrderByRankNumberAsc(
            LocalDate periodStart, LocalDate periodEnd, Pageable pageable);
}
