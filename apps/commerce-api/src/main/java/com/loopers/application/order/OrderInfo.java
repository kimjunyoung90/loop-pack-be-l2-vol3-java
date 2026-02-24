package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;

import java.time.ZonedDateTime;
import java.util.List;

public record OrderInfo(
        Long id,
        Long userId,
        int totalPrice,
        List<OrderItemInfo> orderItems,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static OrderInfo from(Order order) {
        return new OrderInfo(
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                order.getOrderItems().stream()
                        .map(OrderItemInfo::from)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public record OrderItemInfo(
            Long id,
            Long productId,
            String productName,
            int productPrice,
            int quantity,
            int totalPrice,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static OrderItemInfo from(OrderItem orderItem) {
            return new OrderItemInfo(
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
}
