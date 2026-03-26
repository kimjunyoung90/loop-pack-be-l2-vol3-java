package com.loopers.domain.order.event;

import java.util.List;

public record OrderCancelledEvent(
        Long userCouponId,
        List<ItemStock> stockItems
) {
    public record ItemStock(Long productId, int quantity) {
    }
}
