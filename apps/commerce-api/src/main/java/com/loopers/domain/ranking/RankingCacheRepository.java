package com.loopers.domain.ranking;

import java.util.List;

public interface RankingCacheRepository {

    List<Long> getTopRankedProductIds(String date, long offset, long size);

    long getTotalCount(String date);
}
