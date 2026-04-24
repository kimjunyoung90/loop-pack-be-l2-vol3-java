package com.loopers.interfaces.api.ranking.response;

import com.loopers.application.ranking.result.ProductRankWithProductResult;
import com.loopers.domain.common.Money;

import java.time.LocalDate;

public record ProductRankDetailResponse(
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
    public static ProductRankDetailResponse from(ProductRankWithProductResult result) {
        return new ProductRankDetailResponse(
                result.rankNumber(),
                result.productId(),
                result.productName(),
                result.brandName(),
                result.price(),
                result.score(),
                result.totalViewCount(),
                result.totalLikeCount(),
                result.totalSalesCount(),
                result.periodStart(),
                result.periodEnd()
        );
    }
}
