package com.loopers.application.order.result;

import com.loopers.domain.order.OrderItem;

import java.time.ZonedDateTime;

public record OrderItemResult(
        Long id,
        Long productId,
        String productName,
        int productPrice,
        int quantity,
        int totalPrice,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static OrderItemResult from(OrderItem orderItem) {
        return new OrderItemResult(
                orderItem.getId(),
                orderItem.getProductId(),
                orderItem.getProductName(),
                orderItem.getProductPrice(),
                orderItem.getQuantity(),
                orderItem.getTotalPrice(),
                orderItem.getCreatedAt(),
                orderItem.getUpdatedAt()
        );
    }
}
