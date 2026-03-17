package com.loopers.application.product.result;

import com.loopers.domain.product.ProductWithLikeCount;

import java.time.ZonedDateTime;

public record ProductWithLikeCountResult(
        Long id,
        Long brandId,
        String name,
        int price,
        int stock,
        int likeCount,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static ProductWithLikeCountResult from(ProductWithLikeCount product) {
        return new ProductWithLikeCountResult(
                product.id(),
                product.brandId(),
                product.name(),
                product.price(),
                product.stock(),
                product.likeCount(),
                product.createdAt(),
                product.updatedAt()
        );
    }
}
