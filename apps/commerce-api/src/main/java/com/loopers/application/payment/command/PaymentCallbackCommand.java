package com.loopers.application.payment.command;

import com.loopers.domain.payment.PaymentStatus;

public record PaymentCallbackCommand(
        String transactionKey,
        String orderId,
        String cardType,
        String cardNo,
        Long amount,
        PaymentStatus status,
        String reason
) {
}
