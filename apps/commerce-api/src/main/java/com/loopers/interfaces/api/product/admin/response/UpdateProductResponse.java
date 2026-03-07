package com.loopers.interfaces.api.product.admin.response;

import com.loopers.application.product.result.ProductResult;

import java.time.ZonedDateTime;

public record UpdateProductResponse(
        Long id,
        Long brandId,
        String name,
        int price,
        int stock,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static UpdateProductResponse from(ProductResult result) {
        return new UpdateProductResponse(
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
