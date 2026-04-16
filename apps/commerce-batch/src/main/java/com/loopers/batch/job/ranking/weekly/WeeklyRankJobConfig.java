package com.loopers.batch.job.ranking.weekly;

import com.loopers.batch.job.ranking.dto.ProductMetricsAggregation;
import com.loopers.batch.job.ranking.weekly.step.WeeklyRankClearChunkListener;
import com.loopers.batch.job.ranking.weekly.step.WeeklyRankProcessor;
import com.loopers.batch.listener.JobListener;
import com.loopers.batch.listener.StepMonitorListener;
import com.loopers.domain.ranking.MvProductRankWeekly;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = WeeklyRankJobConfig.JOB_NAME)
@RequiredArgsConstructor
@Configuration
public class WeeklyRankJobConfig {

    public static final String JOB_NAME = "weeklyRankJob";
    private static final String STEP_AGGREGATE = "weeklyRankAggregateStep";
    private static final int CHUNK_SIZE = 100;
    private static final int TOP_RANK_LIMIT = 100;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String AGGREGATION_SQL = """
            SELECT sub.rank_number, sub.product_id, sub.score,
                   sub.total_view_count, sub.total_like_count, sub.total_sales_count
            FROM (
                SELECT
                    ROW_NUMBER() OVER (ORDER BY
                        (SUM(view_count) * 0.1 + SUM(like_count) * 0.2 + SUM(sales_count) * 0.6) DESC
                    ) AS rank_number,
                    product_id,
                    (SUM(view_count) * 0.1 + SUM(like_count) * 0.2 + SUM(sales_count) * 0.6) AS score,
                    SUM(view_count) AS total_view_count,
                    SUM(like_count) AS total_like_count,
                    SUM(sales_count) AS total_sales_count
                FROM product_metrics
                WHERE metric_date BETWEEN ? AND ?
                  AND deleted_at IS NULL
                GROUP BY product_id
                ORDER BY score DESC
                LIMIT ?
            ) sub
            """;

    private static final String INSERT_SQL = """
            INSERT INTO mv_product_rank_weekly
                (rank_number, product_id, score, total_view_count, total_like_count, total_sales_count,
                 period_start, period_end, aggregated_at, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            """;

    private final JobRepository jobRepository;
    private final JobListener jobListener;
    private final StepMonitorListener stepMonitorListener;
    private final WeeklyRankClearChunkListener weeklyRankClearChunkListener;
    private final WeeklyRankProcessor weeklyRankProcessor;
    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;

    @Bean(JOB_NAME)
    public Job weeklyRankJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(aggregateStep())
                .listener(jobListener)
                .build();
    }

    @JobScope
    @Bean(STEP_AGGREGATE)
    public Step aggregateStep() {
        return new StepBuilder(STEP_AGGREGATE, jobRepository)
                .<ProductMetricsAggregation, MvProductRankWeekly>chunk(CHUNK_SIZE, transactionManager)
                .reader(weeklyRankReader(null))
                .processor(weeklyRankProcessor)
                .writer(weeklyRankWriter())
                .listener(weeklyRankClearChunkListener)
                .listener(stepMonitorListener)
                .build();
    }

    @StepScope
    @Bean
    public JdbcCursorItemReader<ProductMetricsAggregation> weeklyRankReader(
            @Value("#{jobParameters['requestDate']}") String requestDate
    ) {
        LocalDate baseDate = (requestDate != null) ? LocalDate.parse(requestDate, DATE_FORMATTER) : LocalDate.now();
        LocalDate periodStart = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        LocalDate periodEnd = periodStart.plusDays(6);

        return new JdbcCursorItemReaderBuilder<ProductMetricsAggregation>()
                .name("weeklyRankReader")
                .dataSource(dataSource)
                .sql(AGGREGATION_SQL)
                .preparedStatementSetter(ps -> {
                    ps.setObject(1, periodStart, Types.DATE);
                    ps.setObject(2, periodEnd, Types.DATE);
                    ps.setInt(3, TOP_RANK_LIMIT);
                })
                .rowMapper((rs, rowNum) -> new ProductMetricsAggregation(
                        rs.getInt("rank_number"),
                        rs.getLong("product_id"),
                        rs.getDouble("score"),
                        rs.getLong("total_view_count"),
                        rs.getLong("total_like_count"),
                        rs.getLong("total_sales_count")
                ))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<MvProductRankWeekly> weeklyRankWriter() {
        return new JdbcBatchItemWriterBuilder<MvProductRankWeekly>()
                .dataSource(dataSource)
                .sql(INSERT_SQL)
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setInt(1, item.getRankNumber());
                    ps.setLong(2, item.getProductId());
                    ps.setDouble(3, item.getScore());
                    ps.setLong(4, item.getTotalViewCount());
                    ps.setLong(5, item.getTotalLikeCount());
                    ps.setLong(6, item.getTotalSalesCount());
                    ps.setObject(7, item.getPeriodStart(), Types.DATE);
                    ps.setObject(8, item.getPeriodEnd(), Types.DATE);
                    ps.setObject(9, item.getAggregatedAt());
                })
                .build();
    }
}
