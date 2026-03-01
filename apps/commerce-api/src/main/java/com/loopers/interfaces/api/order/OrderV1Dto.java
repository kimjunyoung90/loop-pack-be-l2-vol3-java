package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderV1Dto {

    public record CreateOrderRequest(
            @NotEmpty @Valid List<CreateOrderItemRequest> orderItems
    ) {}

    public record CreateOrderItemRequest(
            @NotNull Long productId,
            @Min(1) int quantity
    ) {}

    public record OrderResponse(
            Long id,
            Long userId,
            String status,
            int totalPrice,
            List<OrderItemResponse> orderItems,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static OrderResponse from(OrderInfo info) {
            return new OrderResponse(
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
