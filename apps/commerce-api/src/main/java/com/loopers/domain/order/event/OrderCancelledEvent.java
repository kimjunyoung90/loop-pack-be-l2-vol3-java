package com.loopers.domain.order.event;

import java.util.List;

public record OrderCancelledEvent(
        List<ItemStock> stockItems
) {
    public record ItemStock(Long productId, int quantity) {
    }
}
