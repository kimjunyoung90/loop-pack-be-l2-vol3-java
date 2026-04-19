package com.loopers.batch.job.ranking.weekly.step;

import com.loopers.batch.job.ranking.dto.ProductMetricsAggregation;
import com.loopers.batch.job.ranking.weekly.WeeklyRankJobConfig;
import com.loopers.domain.ranking.MvProductRankWeekly;
import jakarta.annotation.Nonnull;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@StepScope
@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = WeeklyRankJobConfig.JOB_NAME)
@Component
public class WeeklyRankProcessor implements ItemProcessor<ProductMetricsAggregation, MvProductRankWeekly> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("#{jobParameters['requestDate']}")
    private String requestDate;

    @Override
    public MvProductRankWeekly process(@Nonnull ProductMetricsAggregation item) {
        LocalDate baseDate = resolveBaseDate();
        LocalDate periodStart = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        LocalDate periodEnd = periodStart.plusDays(6);

        return MvProductRankWeekly.builder()
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
