package com.loopers.application.product;

import com.loopers.domain.product.ProductMetrics;
import com.loopers.domain.product.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductMetricsService {

    private final ProductMetricsRepository productMetricsRepository;

    @Transactional
    public void incrementLikeCount(Long productId) {
        ProductMetrics metrics = getOrCreate(productId);
        metrics.incrementLikeCount();
    }

    @Transactional
    public void decrementLikeCount(Long productId) {
        ProductMetrics metrics = getOrCreate(productId);
        metrics.decrementLikeCount();
    }

    @Transactional
    public void incrementViewCount(Long productId) {
        ProductMetrics metrics = getOrCreate(productId);
        metrics.incrementViewCount();
    }

    @Transactional
    public void incrementSalesCount(Long productId, int quantity) {
        ProductMetrics metrics = getOrCreate(productId);
        metrics.incrementSalesCount(quantity);
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
