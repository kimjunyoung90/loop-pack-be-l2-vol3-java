package com.loopers.interfaces.api.order.response;

import com.loopers.application.order.result.OrderResult.OrderItemResult;
import com.loopers.application.order.result.OrderResult;

import java.time.ZonedDateTime;
import java.util.List;

public record OrderCancelResponse(
        Long id,
        Long userId,
        Long userCouponId,
        String status,
        int totalAmount,
        int discountAmount,
        int finalAmount,
        List<OrderItem> orderItems,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static OrderCancelResponse from(OrderResult result) {
        return new OrderCancelResponse(
                result.id(),
                result.userId(),
                result.userCouponId(),
                result.status(),
                result.totalAmount(),
                result.discountAmount(),
                result.finalAmount(),
                result.orderItems().stream()
                        .map(OrderItem::from)
                        .toList(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    public record OrderItem(
            Long id,
            Long productId,
            String productName,
            int productPrice,
            int quantity,
            int totalPrice,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static OrderItem from(OrderItemResult result) {
            return new OrderItem(
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
}
