package com.loopers.domain.payment.event;

public record PaymentRejectedEvent(
        Long orderId,
        Long userId
) {
}
