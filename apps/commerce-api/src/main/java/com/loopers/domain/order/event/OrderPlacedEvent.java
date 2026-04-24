package com.loopers.domain.order.event;

import com.loopers.domain.common.Money;

import java.util.List;

public record OrderPlacedEvent(
        List<ItemStock> stockItems,
        Long userCouponId,
        Long userId,
        Money totalAmount
) {
    public record ItemStock(Long productId, int quantity) {
    }
}
