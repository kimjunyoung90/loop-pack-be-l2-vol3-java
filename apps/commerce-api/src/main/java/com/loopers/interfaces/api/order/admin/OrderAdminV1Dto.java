package com.loopers.interfaces.api.order.admin;

import com.loopers.application.order.OrderInfo;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderAdminV1Dto {

    public record GetOrderResponse(
            Long id,
            Long userId,
            String status,
            int totalPrice,
            List<OrderItemResponse> orderItems,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static GetOrderResponse from(OrderInfo info) {
            return new GetOrderResponse(
                    info.id(),
                    info.userId(),
                    info.status(),
                    info.totalPrice(),
                    info.orderItems().stream()
                            .map(OrderItemResponse::from)
                            .toList(),
                    info.createdAt(),
                    info.updatedAt()
            );
        }
    }

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
        public static OrderItemResponse from(OrderInfo.OrderItemInfo info) {
            return new OrderItemResponse(
                    info.id(),
                    info.productId(),
                    info.productName(),
                    info.productPrice(),
                    info.quantity(),
                    info.totalPrice(),
                    info.createdAt(),
                    info.updatedAt()
            );
        }
    }
}
