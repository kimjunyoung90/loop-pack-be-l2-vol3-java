package com.loopers.interfaces.api.product.admin.request;

import com.loopers.domain.common.Money;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductUpdateRequest(
        @NotNull
        Long brandId,
        @NotBlank
        String name,
        @NotNull
        Money price,
        @Min(0)
        int stock
) {
}
