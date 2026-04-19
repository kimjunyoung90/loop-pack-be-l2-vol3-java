package com.loopers.application.ranking.result;

import java.util.List;

public record CachedProductRanks(
        List<ProductRankResult> ranks,
        long totalElements
) {
}
