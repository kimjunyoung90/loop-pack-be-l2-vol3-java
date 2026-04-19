package com.loopers.domain.ranking;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "mv_product_rank_monthly",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "period_start", "period_end"})
        },
        indexes = {
                @Index(name = "idx_monthly_period", columnList = "period_start, period_end, rank_number")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MvProductRankMonthly extends MvProductRankBase {

    @Builder
    private MvProductRankMonthly(int rankNumber, Long productId, double score,
                                 long totalViewCount, long totalLikeCount, long totalSalesCount,
                                 LocalDate periodStart, LocalDate periodEnd, ZonedDateTime aggregatedAt) {
        super(rankNumber, productId, score, totalViewCount, totalLikeCount, totalSalesCount,
                periodStart, periodEnd, aggregatedAt);
    }
}
