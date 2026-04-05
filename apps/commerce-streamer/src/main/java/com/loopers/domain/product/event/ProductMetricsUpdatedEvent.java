package com.loopers.domain.product.event;

public record ProductMetricsUpdatedEvent(
        Long productId,
        MetricType metricType,
        int quantity
) {

    public enum MetricType {
        VIEW, LIKE, UNLIKE, SALES
    }
}
