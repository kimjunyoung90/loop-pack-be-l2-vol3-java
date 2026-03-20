package com.loopers.infrastructure.payment.dto;

public record PgMeta(
        String result,
        String errorCode,
        String message
) {
}
