package com.loopers.interfaces.api.payment.request;

public record PaymentCallbackRequest(
        String transactionKey,
        String orderId,
        String cardType,
        String cardNo,
        Long amount,
        String status,
        String reason
) {
}
