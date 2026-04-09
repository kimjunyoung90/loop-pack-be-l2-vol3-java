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

    @Scheduled(cron = "0 0 0 * * *")
    public void aggregateDailyRanking() {
        aggregate(LocalDate.now().minusDays(1));
    }

    public void aggregate(LocalDate date) {
        List<ProductMetrics> metricsList = productMetricsRepository.findAllByMetricDate(date);
        Map<Long, Double> scores = metricsList.stream()
                .collect(Collectors.toMap(
                        ProductMetrics::getProductId,
                        this::calculateScore
                ));

        productRankingRepository.saveScores(date, scores);
        log.info("일간 랭킹 집계 완료: date={}, count={}", date, scores.size());
    }

    private double calculateScore(ProductMetrics metrics) {
        return (metrics.getViewCount() * viewWeight)
                + (metrics.getLikeCount() * likeWeight)
                + (metrics.getSalesCount() * salesWeight);
    }
}
