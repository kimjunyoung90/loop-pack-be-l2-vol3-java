package com.loopers.interfaces.api.brand.response;

import com.loopers.application.brand.result.BrandResult;

public record BrandDetailResponse(
        Long id,
        String name
) {
    public static BrandDetailResponse from(BrandResult brandResult) {
        return new BrandDetailResponse(
                brandResult.id(),
                brandResult.name()
        );
    }
}
