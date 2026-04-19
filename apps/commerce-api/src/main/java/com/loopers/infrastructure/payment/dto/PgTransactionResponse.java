package com.loopers.infrastructure.payment.dto;

public record PgTransactionResponse(
        String transactionKey,
        String status,
        String reason
) {
}
