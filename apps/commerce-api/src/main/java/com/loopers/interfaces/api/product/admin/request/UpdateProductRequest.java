package com.loopers.interfaces.api.product.admin.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
