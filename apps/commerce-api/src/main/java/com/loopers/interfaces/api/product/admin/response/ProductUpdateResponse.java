package com.loopers.interfaces.api.product.admin.response;

import com.loopers.application.product.result.ProductResult;
import com.loopers.domain.common.Money;

import java.time.ZonedDateTime;

public record ProductUpdateResponse(
        Long id,
        Long brandId,
        String name,
        Money price,
        int stock,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static ProductUpdateResponse from(ProductResult result) {
        return new ProductUpdateResponse(
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
