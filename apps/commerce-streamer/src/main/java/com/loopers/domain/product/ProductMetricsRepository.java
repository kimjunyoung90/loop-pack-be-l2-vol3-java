package com.loopers.domain.product;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductMetricsRepository {

    ProductMetrics save(ProductMetrics productMetrics);

    Optional<ProductMetrics> findByProductIdAndMetricDate(Long productId, LocalDate metricDate);

    void upsertLikeCount(Long productId, LocalDate metricDate, int delta);

    void upsertViewCount(Long productId, LocalDate metricDate, int delta);

    void upsertSalesCount(Long productId, LocalDate metricDate, int delta);
}
