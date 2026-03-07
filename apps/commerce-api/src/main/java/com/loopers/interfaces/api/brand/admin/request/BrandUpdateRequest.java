package com.loopers.interfaces.api.brand.admin.request;

import jakarta.validation.constraints.NotBlank;

public record BrandUpdateRequest(
        @NotBlank
        String name
) {
}
