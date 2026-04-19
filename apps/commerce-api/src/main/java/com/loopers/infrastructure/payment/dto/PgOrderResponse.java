package com.loopers.infrastructure.payment.dto;

import java.util.List;

public record PgOrderResponse(
        String orderId,
        List<PgTransactionResponse> transactions
) {
}
