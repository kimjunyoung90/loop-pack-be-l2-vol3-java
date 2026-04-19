package com.loopers.batch.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;


@Slf4j
@RequiredArgsConstructor
@Component
public class JobListener {

    private static final String WEEKLY_CACHE_PREFIX = "ranking:weekly:";
    private static final String MONTHLY_CACHE_PREFIX = "ranking:monthly:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RedisTemplate<String, String> redisTemplate;

    @BeforeJob
    void beforeJob(JobExecution jobExecution) {
        log.info("Job '${jobExecution.jobInstance.jobName}' 시작");
        jobExecution.getExecutionContext().putLong("startTime", System.currentTimeMillis());
    }

    @AfterJob
    void afterJob(JobExecution jobExecution) {
        logExecutionTime(jobExecution);
        evictCacheIfForced(jobExecution);
    }

    private void evictCacheIfForced(JobExecution jobExecution) {
        if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
            return;
        }

        String forceEvict = jobExecution.getJobParameters().getString("forceEvict");
        if (!"true".equals(forceEvict)) {
            return;
        }

        String requestDate = jobExecution.getJobParameters().getString("requestDate");
        LocalDate baseDate = (requestDate != null) ? LocalDate.parse(requestDate, DATE_FORMATTER) : LocalDate.now();

        String jobName = jobExecution.getJobInstance().getJobName();
        boolean isWeekly = jobName.contains("weekly");
        String prefix = isWeekly ? WEEKLY_CACHE_PREFIX : MONTHLY_CACHE_PREFIX;

        LocalDate periodStart;
        if (isWeekly) {
            periodStart = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        } else {
            periodStart = baseDate.minusMonths(1).withDayOfMonth(1);
        }

        String pattern = prefix + periodStart + ":*";
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("캐시 evict 완료: pattern={}, deletedCount={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.warn("캐시 evict 실패: pattern={}", pattern, e);
        }
    }

    private void logExecutionTime(JobExecution jobExecution) {
        var startTime = jobExecution.getExecutionContext().getLong("startTime");
        var endTime = System.currentTimeMillis();

        var startDateTime = Instant.ofEpochMilli(startTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        var endDateTime = Instant.ofEpochMilli(endTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();

        var totalTime = endTime - startTime;
        var duration = Duration.ofMillis(totalTime);
        var hours = duration.toHours();
        var minutes = duration.toMinutes() % 60;
        var seconds = duration.getSeconds() % 60;

        var message = String.format(
            """
                *Start Time:* %s
                *End Time:* %s
                *Total Time:* %d시간 %d분 %d초
            """, startDateTime, endDateTime, hours, minutes, seconds
        ).trim();

        log.info(message);
    }
}
