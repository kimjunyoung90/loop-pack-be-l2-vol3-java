package com.loopers.domain.payment;

public interface PaymentGatewayClient {

    PaymentGatewayResponse requestPayment(PaymentGatewayRequest request);

    record PaymentGatewayRequest(
            Long userId,
            String orderId,
            String cardType,
            String cardNo,
            Long amount,
            String callbackUrl
    ) {
    }

    record PaymentGatewayResponse(
            String transactionKey,
            String status,
            String reason
    ) {
    }
}
