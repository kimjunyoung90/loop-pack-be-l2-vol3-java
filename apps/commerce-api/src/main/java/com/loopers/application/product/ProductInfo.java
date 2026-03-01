package com.loopers.application.product;

import com.loopers.domain.product.Product;

import java.time.ZonedDateTime;

public record ProductInfo(
        Long id,
        Long brandId,
        String name,
        int price,
        int stock,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static ProductInfo from(Product product) {
        return new ProductInfo(
                product.getId(),
                product.getBrand().getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
