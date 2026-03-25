package com.loopers.application.payment.result;

import com.loopers.domain.payment.Payment;

import java.time.ZonedDateTime;

public record PaymentResult(
        Long id,
        Long orderId,
        Long userId,
        String cardType,
        String cardNo,
        Long amount,
        String transactionKey,
        String status,
        String failureReason,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static PaymentResult from(Payment payment) {
        return new PaymentResult(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getCardType().name(),
                payment.getCardNo(),
                payment.getAmount(),
                payment.getTransactionKey(),
                payment.getStatus().name(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
