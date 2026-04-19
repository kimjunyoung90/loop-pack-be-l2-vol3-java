package com.loopers.domain.ranking;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MvProductRankBase extends BaseEntity {

    @Column(name = "rank_number", nullable = false)
    private int rankNumber;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "total_view_count", nullable = false)
    private long totalViewCount;

    @Column(name = "total_like_count", nullable = false)
    private long totalLikeCount;

    @Column(name = "total_sales_count", nullable = false)
    private long totalSalesCount;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "aggregated_at", nullable = false)
    private ZonedDateTime aggregatedAt;

    protected MvProductRankBase(int rankNumber, Long productId, double score,
                                long totalViewCount, long totalLikeCount, long totalSalesCount,
                                LocalDate periodStart, LocalDate periodEnd, ZonedDateTime aggregatedAt) {
        this.rankNumber = rankNumber;
        this.productId = productId;
        this.score = score;
        this.totalViewCount = totalViewCount;
        this.totalLikeCount = totalLikeCount;
        this.totalSalesCount = totalSalesCount;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.aggregatedAt = aggregatedAt;
        guard();
    }

    @Override
    protected void guard() {
        if (productId == null) {
            throw new IllegalArgumentException("productId는 필수입니다.");
        }
        if (rankNumber < 1 || rankNumber > 100) {
            throw new IllegalArgumentException("rankNumber는 1~100 범위여야 합니다.");
        }
        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("periodStart, periodEnd는 필수입니다.");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("periodStart는 periodEnd 이전이어야 합니다.");
        }
        if (aggregatedAt == null) {
            throw new IllegalArgumentException("aggregatedAt은 필수입니다.");
        }
    }
}
