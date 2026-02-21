package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;

import java.time.ZonedDateTime;

public class ProductV1Dto {

    public record GetProductResponse(
            Long id,
            Long brandId,
            String name,
            int price,
            int stock,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static GetProductResponse from(ProductInfo info) {
            return new GetProductResponse(
                    info.id(),
                    info.brandId(),
                    info.name(),
                    info.price(),
                    info.stock(),
                    info.createdAt(),
                    info.updatedAt()
            );
        }
    }
}
