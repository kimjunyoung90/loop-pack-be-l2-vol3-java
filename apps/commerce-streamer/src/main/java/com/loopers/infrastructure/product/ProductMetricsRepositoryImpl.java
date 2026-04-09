package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductMetrics;
import com.loopers.domain.product.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductMetricsRepositoryImpl implements ProductMetricsRepository {

    private final ProductMetricsJpaRepository productMetricsJpaRepository;

    @Override
    public ProductMetrics save(ProductMetrics productMetrics) {
        return productMetricsJpaRepository.save(productMetrics);
    }

    @Override
    public Optional<ProductMetrics> findByProductIdAndMetricDate(Long productId, LocalDate metricDate) {
        return productMetricsJpaRepository.findByProductIdAndMetricDate(productId, metricDate);
    }

    @Override
    public void upsertLikeCount(Long productId, LocalDate metricDate, int delta) {
        productMetricsJpaRepository.upsertLikeCount(productId, metricDate, delta);
    }

    @Override
    public void upsertViewCount(Long productId, LocalDate metricDate, int delta) {
        productMetricsJpaRepository.upsertViewCount(productId, metricDate, delta);
    }

    @Override
    public void upsertSalesCount(Long productId, LocalDate metricDate, int delta) {
        productMetricsJpaRepository.upsertSalesCount(productId, metricDate, delta);
    }
}
