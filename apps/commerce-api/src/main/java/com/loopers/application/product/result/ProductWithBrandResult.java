package com.loopers.application.product.result;

import com.loopers.domain.common.Money;

import java.time.ZonedDateTime;

public record ProductWithBrandResult(
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
    public static ProductWithBrandResult from(ProductResult product, String brandName) {
        return from(product, brandName, null);
    }

    public static ProductWithBrandResult from(ProductResult product, String brandName, Long rank) {
        return new ProductWithBrandResult(
                product.id(),
                product.brandId(),
                brandName,
                product.name(),
                product.price(),
                product.stock(),
                product.likeCount(),
                rank != null ? rank.intValue() : null,
                product.createdAt(),
                product.updatedAt()
        );
    }
}
