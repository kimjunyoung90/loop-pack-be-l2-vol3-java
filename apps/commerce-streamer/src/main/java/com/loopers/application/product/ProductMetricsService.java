package com.loopers.application.product;

import com.loopers.domain.product.ProductMetricsRepository;
import com.loopers.domain.product.event.ProductMetricsUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class ProductMetricsService {

    private final ProductMetricsRepository productMetricsRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void incrementLikeCount(Long productId) {
        productMetricsRepository.upsertLikeCount(productId, LocalDate.now(), 1);
        eventPublisher.publishEvent(new ProductMetricsUpdatedEvent(productId, ProductMetricsUpdatedEvent.MetricType.LIKE, 1));
    }

    @Transactional
    public void decrementLikeCount(Long productId) {
        productMetricsRepository.upsertLikeCount(productId, LocalDate.now(), -1);
        eventPublisher.publishEvent(new ProductMetricsUpdatedEvent(productId, ProductMetricsUpdatedEvent.MetricType.UNLIKE, 1));
    }

    @Transactional
    public void incrementViewCount(Long productId) {
        productMetricsRepository.upsertViewCount(productId, LocalDate.now(), 1);
        eventPublisher.publishEvent(new ProductMetricsUpdatedEvent(productId, ProductMetricsUpdatedEvent.MetricType.VIEW, 1));
    }

    @Transactional
    public void incrementSalesCount(Long productId, int quantity) {
        productMetricsRepository.upsertSalesCount(productId, LocalDate.now(), quantity);
        eventPublisher.publishEvent(new ProductMetricsUpdatedEvent(productId, ProductMetricsUpdatedEvent.MetricType.SALES, quantity));
    }
}
