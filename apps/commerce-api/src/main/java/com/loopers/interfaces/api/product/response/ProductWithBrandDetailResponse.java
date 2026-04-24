package com.loopers.interfaces.api.product.response;

import com.loopers.application.product.result.ProductWithBrandResult;
import com.loopers.domain.common.Money;

import java.time.ZonedDateTime;

public record ProductWithBrandDetailResponse(
        Long id,
        Long brandId,
        String brandName,
        String name,
        Money price,
        int stock,
        int likeCount,
        Integer rank,
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
                result.rank(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
