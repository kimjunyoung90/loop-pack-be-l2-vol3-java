package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ProductRankRepository {

    Page<MvProductRankWeekly> findWeeklyRanks(LocalDate date, Pageable pageable);

    Page<MvProductRankMonthly> findMonthlyRanks(LocalDate date, Pageable pageable);

    Page<MvProductRankWeekly> findLatestWeeklyRanks(LocalDate date, Pageable pageable);

    Page<MvProductRankMonthly> findLatestMonthlyRanks(LocalDate date, Pageable pageable);
}
