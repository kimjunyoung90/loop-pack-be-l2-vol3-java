package com.loopers.application.payment.command;

import com.loopers.domain.payment.PgCallbackStatus;

public record PaymentCallbackCommand(
        String transactionKey,
        String orderId,
        String cardType,
        String cardNo,
        Long amount,
        PgCallbackStatus status,
        String reason
) {
}
