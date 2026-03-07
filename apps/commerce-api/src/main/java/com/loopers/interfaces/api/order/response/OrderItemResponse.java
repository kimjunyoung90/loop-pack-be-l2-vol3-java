package com.loopers.interfaces.api.order.response;

import com.loopers.application.order.result.OrderItemResult;

import java.time.ZonedDateTime;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        int productPrice,
        int quantity,
        int totalPrice,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static OrderItemResponse from(OrderItemResult result) {
        return new OrderItemResponse(
                result.id(),
                result.productId(),
                result.productName(),
                result.productPrice(),
                result.quantity(),
                result.totalPrice(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
