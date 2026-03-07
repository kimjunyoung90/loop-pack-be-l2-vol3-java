package com.loopers.interfaces.api.brand.response;

import com.loopers.application.brand.result.BrandResult;

public record BrandGetResponse(
        Long id,
        String name
) {
    public static BrandGetResponse from(BrandResult brandResult) {
        return new BrandGetResponse(
                brandResult.id(),
                brandResult.name()
        );
    }
}
