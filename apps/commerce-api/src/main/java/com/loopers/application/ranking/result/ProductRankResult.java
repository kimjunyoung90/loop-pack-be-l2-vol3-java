package com.loopers.application.ranking.result;

import com.loopers.domain.ranking.MvProductRankBase;

import java.time.LocalDate;

public record ProductRankResult(
        int rankNumber,
        Long productId,
        double score,
        long totalViewCount,
        long totalLikeCount,
        long totalSalesCount,
        LocalDate periodStart,
        LocalDate periodEnd
) {
    public static ProductRankResult from(MvProductRankBase entity) {
        return new ProductRankResult(
                entity.getRankNumber(),
                entity.getProductId(),
                entity.getScore(),
                entity.getTotalViewCount(),
                entity.getTotalLikeCount(),
                entity.getTotalSalesCount(),
                entity.getPeriodStart(),
                entity.getPeriodEnd()
        );
    }
}
