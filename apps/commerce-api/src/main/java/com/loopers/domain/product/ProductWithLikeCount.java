package com.loopers.domain.product;

import java.time.ZonedDateTime;

public record ProductWithLikeCount(
        Long id,
        Long brandId,
        String name,
        int price,
        int stock,
        int likeCount,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
}
