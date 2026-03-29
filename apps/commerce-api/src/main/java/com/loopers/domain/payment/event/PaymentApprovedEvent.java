package com.loopers.domain.payment.event;

public record PaymentApprovedEvent(
        Long orderId
) {
}
