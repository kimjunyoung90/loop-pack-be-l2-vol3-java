package com.loopers.application.order.command;

import java.util.List;

public record OrderCreateCommand(
        Long userId,
        String idempotencyKey,
        Long userCouponId,
        List<OrderItem> orderItems
) {
    public record OrderItem(
            Long productId,
            int quantity
    ) {
    }
}
