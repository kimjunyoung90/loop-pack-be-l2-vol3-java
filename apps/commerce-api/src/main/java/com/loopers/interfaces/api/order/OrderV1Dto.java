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
            @NotNull Long userId,
            @NotEmpty @Valid List<CreateOrderItemRequest> orderItems
    ) {}

    public record CreateOrderItemRequest(
            @NotNull Long productId,
            @Min(1) int quantity
    ) {}

    public record CreateOrderResponse(
            Long id,
            Long userId,
            int totalPrice,
            List<OrderItemResponse> orderItems,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static CreateOrderResponse from(OrderInfo info) {
            return new CreateOrderResponse(
                    info.id(),
                    info.userId(),
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
