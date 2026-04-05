package com.loopers.application.product;

import com.loopers.domain.product.ProductRankingRepository;
import com.loopers.domain.product.event.ProductMetricsUpdatedEvent;
import com.loopers.domain.product.event.ProductMetricsUpdatedEvent.MetricType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class ProductRankingService {

    private final ProductRankingRepository productRankingRepository;
    private final Map<Long, Double> buffer = new ConcurrentHashMap<>();

    @Value("${ranking.weights.view:0.1}")
    private double viewWeight;

    @Value("${ranking.weights.like:0.2}")
    private double likeWeight;

    @Value("${ranking.weights.sales:0.6}")
    private double salesWeight;

    @EventListener
    public void handleMetricsUpdated(ProductMetricsUpdatedEvent event) {
        double score = calculateScore(event.metricType(), event.quantity());
        buffer.merge(event.productId(), score, Double::sum);
    }

    public void flush() {
        if (buffer.isEmpty()) return;
        productRankingRepository.incrementScores(buffer);
        buffer.clear();
    }

    private double calculateScore(MetricType metricType, int quantity) {
        return switch (metricType) {
            case VIEW -> viewWeight * quantity;
            case LIKE -> likeWeight * quantity;
            case UNLIKE -> -likeWeight * quantity;
            case SALES -> salesWeight * quantity;
        };
    }
}
