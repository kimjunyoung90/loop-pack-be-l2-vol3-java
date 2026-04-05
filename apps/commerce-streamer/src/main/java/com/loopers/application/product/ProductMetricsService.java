package com.loopers.application.product;

import com.loopers.domain.product.ProductMetrics;
import com.loopers.domain.product.ProductMetricsRepository;
import com.loopers.domain.product.event.ProductMetricsUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductMetricsService {

    private final ProductMetricsRepository productMetricsRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void incrementLikeCount(Long productId) {
        ProductMetrics metrics = getOrCreate(productId);
        metrics.incrementLikeCount();
        eventPublisher.publishEvent(new ProductMetricsUpdatedEvent(productId, ProductMetricsUpdatedEvent.MetricType.LIKE, 1));
    }

    @Transactional
    public void decrementLikeCount(Long productId) {
        ProductMetrics metrics = getOrCreate(productId);
        metrics.decrementLikeCount();
        eventPublisher.publishEvent(new ProductMetricsUpdatedEvent(productId, ProductMetricsUpdatedEvent.MetricType.UNLIKE, 1));
    }

    @Transactional
    public void incrementViewCount(Long productId) {
        ProductMetrics metrics = getOrCreate(productId);
        metrics.incrementViewCount();
        eventPublisher.publishEvent(new ProductMetricsUpdatedEvent(productId, ProductMetricsUpdatedEvent.MetricType.VIEW, 1));
    }

    @Transactional
    public void incrementSalesCount(Long productId, int quantity) {
        ProductMetrics metrics = getOrCreate(productId);
        metrics.incrementSalesCount(quantity);
        eventPublisher.publishEvent(new ProductMetricsUpdatedEvent(productId, ProductMetricsUpdatedEvent.MetricType.SALES, quantity));
    }

    private ProductMetrics getOrCreate(Long productId) {
        return productMetricsRepository.findByProductId(productId)
                .orElseGet(() -> productMetricsRepository.save(
                        ProductMetrics.builder()
                                .productId(productId)
                                .build()
                ));
    }
}
