package com.loopers.interfaces.api.product.admin.response;

import com.loopers.application.product.result.ProductResult;

import java.time.ZonedDateTime;

public record ProductCreateResponse(
        Long id,
        Long brandId,
        String name,
        int price,
        int stock,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static ProductCreateResponse from(ProductResult result) {
        return new ProductCreateResponse(
                result.id(),
                result.brandId(),
                result.name(),
                result.price(),
                result.stock(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
