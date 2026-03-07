package com.loopers.application.order.command;

public record OrderItemCreateCommand(
        Long productId,
        int quantity
) {
}
