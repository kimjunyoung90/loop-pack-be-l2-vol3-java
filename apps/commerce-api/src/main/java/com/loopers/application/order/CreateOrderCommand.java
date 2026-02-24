package com.loopers.application.order;

import java.util.List;

public record CreateOrderCommand(
        Long userId,
        List<CreateOrderItemCommand> orderItems
) {
    public record CreateOrderItemCommand(
            Long productId,
            int quantity
    ) {}
}
