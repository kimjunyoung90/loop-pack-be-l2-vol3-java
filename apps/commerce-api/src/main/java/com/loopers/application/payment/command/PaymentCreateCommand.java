package com.loopers.application.payment.command;

public record PaymentCreateCommand(
        Long userId,
        Long orderId,
        String cardType,
        String cardNo,
        Long amount
) {
}
