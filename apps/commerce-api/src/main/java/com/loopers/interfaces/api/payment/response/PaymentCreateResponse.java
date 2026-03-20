package com.loopers.interfaces.api.payment.response;

import com.loopers.application.payment.result.PaymentResult;

import java.time.ZonedDateTime;

public record PaymentCreateResponse(
        Long id,
        Long orderId,
        String transactionKey,
        String status,
        ZonedDateTime createdAt
) {
    public static PaymentCreateResponse from(PaymentResult result) {
        return new PaymentCreateResponse(
                result.id(),
                result.orderId(),
                result.transactionKey(),
                result.status(),
                result.createdAt()
        );
    }
}
