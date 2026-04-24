package com.loopers.interfaces.api.order.response;

import com.loopers.application.order.result.OrderResult.OrderItemResult;
import com.loopers.application.order.result.OrderResult;
import com.loopers.domain.common.Money;

import java.time.ZonedDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        Long userId,
        Long userCouponId,
        String status,
        Money totalAmount,
        Money discountAmount,
        Money finalAmount,
        List<OrderItemResponse> orderItems,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static OrderDetailResponse from(OrderResult result) {
        return new OrderDetailResponse(
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

    public record OrderItemResponse(
            Long id,
            Long productId,
            String productName,
            Money productPrice,
            int quantity,
            Money totalPrice,
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
}
