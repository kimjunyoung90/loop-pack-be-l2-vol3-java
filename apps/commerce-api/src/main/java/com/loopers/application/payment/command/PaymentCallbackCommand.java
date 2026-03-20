package com.loopers.application.payment.command;

public record PaymentCallbackCommand(
        String transactionKey,
        String orderId,
        String cardType,
        String cardNo,
        Long amount,
        String status,
        String reason
) {
}
