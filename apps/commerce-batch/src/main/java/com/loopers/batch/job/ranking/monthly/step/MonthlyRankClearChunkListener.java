package com.loopers.batch.job.ranking.monthly.step;

import com.loopers.batch.job.ranking.monthly.MonthlyRankJobConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
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
public class MonthlyRankClearChunkListener implements ChunkListener {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JdbcTemplate jdbcTemplate;

    @Value("#{jobParameters['requestDate']}")
    private String requestDate;

    private boolean cleared = false;

    @Override
    public void beforeChunk(ChunkContext context) {
        if (cleared) {
            return;
        }

        LocalDate baseDate = (requestDate != null) ? LocalDate.parse(requestDate, DATE_FORMATTER) : LocalDate.now();
        LocalDate periodStart = baseDate.minusMonths(1).withDayOfMonth(1);
        LocalDate periodEnd = periodStart.with(TemporalAdjusters.lastDayOfMonth());

        int deleted = jdbcTemplate.update(
                "DELETE FROM mv_product_rank_monthly WHERE period_start = ? AND period_end = ?",
                periodStart, periodEnd
        );

        cleared = true;
        log.info("월간 랭킹 기존 데이터 삭제 완료: periodStart={}, periodEnd={}, deletedCount={}", periodStart, periodEnd, deleted);
    }
}
