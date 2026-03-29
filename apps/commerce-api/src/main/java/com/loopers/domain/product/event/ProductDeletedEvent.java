package com.loopers.domain.product.event;

import java.util.List;

public record ProductDeletedEvent(List<Long> productIds) {

    public static ProductDeletedEvent of(Long productId) {
        return new ProductDeletedEvent(List.of(productId));
    }
}
