package com.loopers.application.ranking.result;

import java.time.LocalDate;

public record ProductRankWithProductResult(
        int rankNumber,
        Long productId,
        String productName,
        String brandName,
        int price,
        double score,
        long totalViewCount,
        long totalLikeCount,
        long totalSalesCount,
        LocalDate periodStart,
        LocalDate periodEnd
) {
    public static ProductRankWithProductResult from(ProductRankResult rank, String productName, String brandName, int price) {
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
