package com.loopers.domain.order.event;

import java.util.List;

public record OrderPlacedEvent(
        List<ItemStock> stockItems,
        Long userCouponId,
        Long userId,
        int totalAmount
) {
    public record ItemStock(Long productId, int quantity) {
    }
}
