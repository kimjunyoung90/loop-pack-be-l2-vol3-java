package com.loopers.batch.job.ranking.dto;

public record ProductMetricsAggregation(
        int rankNumber,
        Long productId,
        double score,
        long totalViewCount,
        long totalLikeCount,
        long totalSalesCount
) {
}
