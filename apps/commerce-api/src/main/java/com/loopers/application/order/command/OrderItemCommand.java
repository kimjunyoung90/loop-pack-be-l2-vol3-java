package com.loopers.application.order.command;

import java.util.List;

public record OrderItemCommand(
        Long productId,
        String productName,
        int productPrice,
        int quantity
) {
    public static int calculateTotalAmount(List<OrderItemCommand> items) {
        return items.stream()
                .mapToInt(item -> item.productPrice() * item.quantity())
                .sum();
    }
}
