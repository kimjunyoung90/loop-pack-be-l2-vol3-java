package com.loopers.batch.job.ranking.monthly.step;

import com.loopers.batch.job.ranking.monthly.MonthlyRankJobConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@StepScope
@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = MonthlyRankJobConfig.JOB_NAME)
@RequiredArgsConstructor
@Component
public class MonthlyRankValidationTasklet implements Tasklet {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String COUNT_SQL =
            "SELECT COUNT(DISTINCT metric_date) FROM product_metrics WHERE metric_date BETWEEN ? AND ? AND deleted_at IS NULL";

    private final JdbcTemplate jdbcTemplate;

    @Value("#{jobParameters['requestDate']}")
    private String requestDate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate baseDate = (requestDate != null) ? LocalDate.parse(requestDate, DATE_FORMATTER) : LocalDate.now();
        LocalDate periodStart = baseDate.minusMonths(1).withDayOfMonth(1);
        LocalDate periodEnd = periodStart.with(TemporalAdjusters.lastDayOfMonth());

        Integer dateCount = jdbcTemplate.queryForObject(COUNT_SQL, Integer.class, periodStart, periodEnd);
        int expectedDays = periodEnd.getDayOfMonth();

        log.info("월간 랭킹 사전 검증: period={} ~ {}, 집계된 일수={}/{}",
                periodStart, periodEnd, dateCount, expectedDays);

        if (dateCount == null || dateCount == 0) {
            throw new IllegalStateException(
                    String.format("월간 랭킹 사전 검증 실패: product_metrics에 %s ~ %s 기간 데이터가 존재하지 않습니다",
                            periodStart, periodEnd));
        }

        if (dateCount < expectedDays / 2) {
            log.warn("월간 랭킹 데이터 부족 경고: 기대 {}일 중 {}일만 존재 (period={} ~ {})",
                    expectedDays, dateCount, periodStart, periodEnd);
        }

        return RepeatStatus.FINISHED;
    }
}
