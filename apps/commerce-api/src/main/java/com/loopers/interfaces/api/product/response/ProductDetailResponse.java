package com.loopers.interfaces.api.product.response;

import com.loopers.application.product.result.ProductResult;

import java.time.ZonedDateTime;

public record ProductDetailResponse(
        Long id,
        Long brandId,
        String brandName,
        String name,
        int price,
        int stock,
        int likeCount,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static ProductDetailResponse from(ProductResult result) {
        return new ProductDetailResponse(
                result.id(),
                result.brandId(),
                result.brandName(),
                result.name(),
                result.price(),
                result.stock(),
                result.likeCount(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}