package com.loopers.application.order.command;

import com.loopers.domain.common.Money;

import java.util.List;

public record OrderItemCommand(
        Long productId,
        String productName,
        Money productPrice,
        int quantity
) {
    public static Money calculateTotalAmount(List<OrderItemCommand> items) {
        return items.stream()
                .map(item -> item.productPrice().multiply(item.quantity()))
                .reduce(Money.ZERO, Money::add);
    }
}
