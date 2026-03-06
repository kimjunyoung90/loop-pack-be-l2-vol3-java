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
            @NotEmpty @Valid List<CreateOrderItemRequest> orderItems,
            Long userCouponId
    ) {}

    public record CreateOrderItemRequest(
            @NotNull Long productId,
            @Min(1) int quantity
    ) {}

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
        public static CreateOrderResponse from(OrderInfo info) {
            return new CreateOrderResponse(
                    info.id(),
                    info.userId(),
                    info.userCouponId(),
                    info.status(),
                    info.totalAmount(),
                    info.discountAmount(),
                    info.finalAmount(),
                    info.orderItems().stream()
                            .map(OrderItemResponse::from)
                            .toList(),
                    info.createdAt(),
                    info.updatedAt()
            );
        }
    }

    public record CancelOrderResponse(
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
        public static CancelOrderResponse from(OrderInfo info) {
            return new CancelOrderResponse(
                    info.id(),
                    info.userId(),
                    info.userCouponId(),
                    info.status(),
                    info.totalAmount(),
                    info.discountAmount(),
                    info.finalAmount(),
                    info.orderItems().stream()
                            .map(OrderItemResponse::from)
                            .toList(),
                    info.createdAt(),
                    info.updatedAt()
            );
        }
    }

    public record GetOrderResponse(
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
        public static GetOrderResponse from(OrderInfo info) {
            return new GetOrderResponse(
                    info.id(),
                    info.userId(),
                    info.userCouponId(),
                    info.status(),
                    info.totalAmount(),
                    info.discountAmount(),
                    info.finalAmount(),
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
