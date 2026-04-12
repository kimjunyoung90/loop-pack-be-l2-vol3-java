package com.loopers.infrastructure.kafka.message;

import com.loopers.application.order.command.OrderCreateCommand;

import java.util.List;

public record OrderRequestMessage(
        Long userId,
        String idempotencyKey,
        Long userCouponId,
        List<OrderItem> orderItems
) {
    public record OrderItem(Long productId, int quantity) {
    }

    public static OrderRequestMessage from(OrderCreateCommand command) {
        return new OrderRequestMessage(
                command.userId(),
                command.idempotencyKey(),
                command.userCouponId(),
                command.orderItems().stream()
                        .map(item -> new OrderItem(item.productId(), item.quantity()))
                        .toList()
        );
    }

    public OrderCreateCommand toCommand() {
        return new OrderCreateCommand(
                userId,
                idempotencyKey,
                userCouponId,
                orderItems.stream()
                        .map(item -> new OrderCreateCommand.OrderItem(item.productId(), item.quantity()))
                        .toList()
        );
    }
}
