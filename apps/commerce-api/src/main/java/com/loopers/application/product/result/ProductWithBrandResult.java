package com.loopers.application.product.result;

import java.time.ZonedDateTime;

public record ProductWithBrandResult(
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
    public static ProductWithBrandResult from(ProductResult product, String brandName, int likeCount) {
        return new ProductWithBrandResult(
                product.id(),
                product.brandId(),
                brandName,
                product.name(),
                product.price(),
                product.stock(),
                likeCount,
                product.createdAt(),
                product.updatedAt()
        );
    }
}
