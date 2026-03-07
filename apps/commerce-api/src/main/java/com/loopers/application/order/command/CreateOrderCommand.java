package com.loopers.application.order.command;

import java.util.List;

public record CreateOrderCommand(
        Long userId,
        Long userCouponId,
        List<CreateOrderItemCommand> orderItems
) {
}
