package com.loopers.domain.ranking;

import com.loopers.domain.ranking.MvProductRankWeekly;
import com.loopers.domain.ranking.MvProductRankMonthly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRankRepository {

    Page<MvProductRankWeekly> findLatestWeeklyRanks(Pageable pageable);

    Page<MvProductRankMonthly> findLatestMonthlyRanks(Pageable pageable);
}
