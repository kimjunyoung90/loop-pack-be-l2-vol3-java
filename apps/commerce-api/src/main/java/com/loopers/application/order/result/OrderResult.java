package com.loopers.application.order.result;

import com.loopers.domain.order.Order;

import java.time.ZonedDateTime;
import java.util.List;

public record OrderResult(
        Long id,
        Long userId,
        Long userCouponId,
        String status,
        int totalAmount,
        int discountAmount,
        int finalAmount,
        List<OrderItemResult> orderItems,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static OrderResult from(Order order) {
        return new OrderResult(
                order.getId(),
                order.getUserId(),
                order.getUserCouponId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getFinalAmount(),
                order.getOrderItems().stream()
                        .map(OrderItemResult::from)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
