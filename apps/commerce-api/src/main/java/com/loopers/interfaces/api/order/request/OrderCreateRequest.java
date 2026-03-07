package com.loopers.interfaces.api.order.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(
        @NotEmpty @Valid List<OrderItem> orderItems,
        Long userCouponId
) {
    public record OrderItem(
            @NotNull Long productId,
            @Min(1) int quantity
    ) {
    }
}
