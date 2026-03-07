package com.loopers.interfaces.api.order.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequest(
        @NotEmpty @Valid List<OrderItemCreateRequest> orderItems,
        Long userCouponId
) {
}
