package com.loopers.domain.payment;

import java.util.Optional;

public interface PaymentGatewayClient {

    PaymentGatewayResponse requestPayment(PaymentGatewayRequest request);

    Optional<ReconciliationResult> findByOrderId(Long userId, Long orderId);

    enum PgResponseStatus {
        PENDING,
        REJECTED
    }

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
            PgResponseStatus status,
            String reason
    ) {
    }

    record ReconciliationResult(
            String transactionKey,
            PaymentStatus status,
            String reason
    ) {
    }
}
