package com.loopers.application.order.command;

import java.util.List;

public record OrderCreateCommand(
        Long userId,
        Long userCouponId,
        List<OrderItemCreateCommand> orderItems
) {
}
