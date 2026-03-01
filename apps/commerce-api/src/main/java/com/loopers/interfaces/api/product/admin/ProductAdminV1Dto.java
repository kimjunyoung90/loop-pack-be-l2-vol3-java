package com.loopers.interfaces.api.product.admin;

import com.loopers.application.product.ProductInfo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public class ProductAdminV1Dto {

    public record CreateProductRequest(
            @NotNull
            Long brandId,
            @NotBlank
            String name,
            @Min(1)
            int price,
            @Min(0)
            int stock
    ) {
    }

    public record ProductResponse(
            Long id,
            Long brandId,
            String name,
            int price,
            int stock,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static ProductResponse from(ProductInfo info) {
            return new ProductResponse(
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

    public record UpdateProductRequest(
            @NotNull
            Long brandId,
            @NotBlank
            String name,
            @Min(1)
            int price,
            @Min(0)
            int stock
    ) {
    }
}
