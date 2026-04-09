package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductMetrics;
import com.loopers.domain.product.ProductMetricsRepository;
import com.loopers.domain.product.ProductRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class RankingScheduler {

    private final ProductMetricsRepository productMetricsRepository;
    private final ProductRankingRepository productRankingRepository;

    @Value("${ranking.weights.view:0.1}")
    private double viewWeight;

    @Value("${ranking.weights.like:0.2}")
    private double likeWeight;

    @Value("${ranking.weights.sales:0.6}")
    private double salesWeight;

    @Value("${ranking.carry-over-rate:0.3}")
    private double carryOverRate;

    @Scheduled(cron = "0 50 23 * * *")
    public void carryOverRanking() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        Map<Long, Double> todayScores = productRankingRepository.getAllScores(today);
        Map<Long, Double> carryOverScores = todayScores.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() * carryOverRate
                ));

        productRankingRepository.saveScores(tomorrow, carryOverScores);
        log.info("랭킹 carry-over 완료: from={}, to={}, count={}", today, tomorrow, carryOverScores.size());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void aggregateDailyRanking() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();

        List<ProductMetrics> metricsList = productMetricsRepository.findAllByMetricDate(yesterday);
        Map<Long, Double> scores = metricsList.stream()
                .collect(Collectors.toMap(
                        ProductMetrics::getProductId,
                        this::calculateScore
                ));

        productRankingRepository.incrementScores(today, scores);
        log.info("일간 랭킹 집계 완료: metricDate={}, targetDate={}, count={}", yesterday, today, scores.size());
    }

	private double calculateScore(ProductMetrics metrics) {
        return (metrics.getViewCount() * viewWeight)
                + (metrics.getLikeCount() * likeWeight)
                + (metrics.getSalesCount() * salesWeight);
    }
}
