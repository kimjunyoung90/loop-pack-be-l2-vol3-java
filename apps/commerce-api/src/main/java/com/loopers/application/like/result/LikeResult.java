package com.loopers.application.like.result;

import com.loopers.domain.like.ProductLike;

import java.time.ZonedDateTime;

public record LikeResult(
        Long id,
        Long userId,
        Long productId,
        ZonedDateTime createdAt
) {
    public static LikeResult from(ProductLike productLike) {
        return new LikeResult(
                productLike.getId(),
                productLike.getUserId(),
                productLike.getProductId(),
                productLike.getCreatedAt()
        );
    }
}
