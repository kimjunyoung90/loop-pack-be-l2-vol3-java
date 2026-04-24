package com.loopers.application.ranking.result;

import com.loopers.domain.common.Money;

import java.time.LocalDate;

public record ProductRankWithProductResult(
        int rankNumber,
        Long productId,
        String productName,
        String brandName,
        Money price,
        double score,
        long totalViewCount,
        long totalLikeCount,
        long totalSalesCount,
        LocalDate periodStart,
        LocalDate periodEnd
) {
    public static ProductRankWithProductResult from(ProductRankResult rank, String productName, String brandName, Money price) {
        return new ProductRankWithProductResult(
                rank.rankNumber(),
                rank.productId(),
                productName,
                brandName,
                price,
                rank.score(),
                rank.totalViewCount(),
                rank.totalLikeCount(),
                rank.totalSalesCount(),
                rank.periodStart(),
                rank.periodEnd()
        );
    }
}
