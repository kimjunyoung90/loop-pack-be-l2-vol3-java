package com.loopers.interfaces.api.order.response;

import com.loopers.application.order.result.OrderResult;

import java.time.ZonedDateTime;
import java.util.List;

public record CreateOrderResponse(
        Long id,
        Long userId,
        Long userCouponId,
        String status,
        int totalAmount,
        int discountAmount,
        int finalAmount,
        List<OrderItemResponse> orderItems,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static CreateOrderResponse from(OrderResult result) {
        return new CreateOrderResponse(
                result.id(),
                result.userId(),
                result.userCouponId(),
                result.status(),
                result.totalAmount(),
                result.discountAmount(),
                result.finalAmount(),
                result.orderItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
