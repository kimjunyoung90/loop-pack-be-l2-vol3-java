package com.loopers.batch.job.ranking.monthly.step;

import com.loopers.batch.job.ranking.dto.ProductMetricsAggregation;
import com.loopers.batch.job.ranking.monthly.MonthlyRankJobConfig;
import com.loopers.domain.ranking.MvProductRankMonthly;
import jakarta.annotation.Nonnull;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@StepScope
@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = MonthlyRankJobConfig.JOB_NAME)
@Component
public class MonthlyRankProcessor implements ItemProcessor<ProductMetricsAggregation, MvProductRankMonthly> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("#{jobParameters['requestDate']}")
    private String requestDate;

    @Override
    public MvProductRankMonthly process(@Nonnull ProductMetricsAggregation item) {
        LocalDate baseDate = resolveBaseDate();
        LocalDate periodStart = baseDate.minusMonths(1).withDayOfMonth(1);
        LocalDate periodEnd = periodStart.with(TemporalAdjusters.lastDayOfMonth());

        return MvProductRankMonthly.builder()
                .rankNumber(item.rankNumber())
                .productId(item.productId())
                .score(item.score())
                .totalViewCount(item.totalViewCount())
                .totalLikeCount(item.totalLikeCount())
                .totalSalesCount(item.totalSalesCount())
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .aggregatedAt(ZonedDateTime.now())
                .build();
    }

    private LocalDate resolveBaseDate() {
        return (requestDate != null) ? LocalDate.parse(requestDate, DATE_FORMATTER) : LocalDate.now();
    }
}
