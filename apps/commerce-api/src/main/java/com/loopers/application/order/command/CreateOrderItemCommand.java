package com.loopers.application.order.command;

public record CreateOrderItemCommand(
        Long productId,
        int quantity
) {
}
