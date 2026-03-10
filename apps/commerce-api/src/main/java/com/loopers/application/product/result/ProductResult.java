package com.loopers.application.product.result;

import com.loopers.domain.product.Product;

import java.time.ZonedDateTime;

public record ProductResult(
        Long id,
        Long brandId,
        String name,
        int price,
        int stock,
        int likeCount,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static ProductResult from(Product product) {
        return new ProductResult(
                product.getId(),
                product.getBrandId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getLikeCount(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}