package com.loopers.interfaces.api.brand.response;

import com.loopers.application.brand.result.BrandResult;

public record GetBrandResponse(
        Long id,
        String name
) {
    public static GetBrandResponse from(BrandResult brandResult) {
        return new GetBrandResponse(
                brandResult.id(),
                brandResult.name()
        );
    }
}
