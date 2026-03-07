package com.loopers.interfaces.api.brand.admin.request;

import jakarta.validation.constraints.NotBlank;

public record BrandCreateRequest(
        @NotBlank
        String name
) {
}
