package com.loopers.infrastructure.payment.dto;

public record PgApiResponse<T>(
        PgMeta meta,
        T data
) {
}
