package com.loopers.interfaces.api.product.response;

import com.loopers.application.product.result.ProductWithBrandResult;

import java.time.ZonedDateTime;

public record ProductWithBrandDetailResponse(
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
    public static ProductWithBrandDetailResponse from(ProductWithBrandResult result) {
        return new ProductWithBrandDetailResponse(
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
