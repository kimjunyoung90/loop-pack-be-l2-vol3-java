package com.loopers.application.like;

import com.loopers.domain.like.ProductLike;

import java.time.ZonedDateTime;

public record LikeInfo(
        Long id,
        Long userId,
        Long productId,
        ZonedDateTime createdAt
) {
    public static LikeInfo from(ProductLike productLike) {
        return new LikeInfo(
                productLike.getId(),
                productLike.getUserId(),
                productLike.getProductId(),
                productLike.getCreatedAt()
        );
    }
}
