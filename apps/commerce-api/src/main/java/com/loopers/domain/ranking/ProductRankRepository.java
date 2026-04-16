package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ProductRankRepository {

    Page<MvProductRankWeekly> findWeeklyRanks(LocalDate periodStart, LocalDate periodEnd, Pageable pageable);

    Page<MvProductRankMonthly> findMonthlyRanks(LocalDate periodStart, LocalDate periodEnd, Pageable pageable);
}
