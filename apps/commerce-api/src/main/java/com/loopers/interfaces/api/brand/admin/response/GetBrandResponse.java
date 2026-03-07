package com.loopers.interfaces.api.brand.admin.response;

import com.loopers.application.brand.result.BrandResult;

import java.time.ZonedDateTime;

public record GetBrandResponse(
        Long id,
        String name,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static GetBrandResponse from(BrandResult brandResult) {
        return new GetBrandResponse(
                brandResult.id(),
                brandResult.name(),
                brandResult.createdAt(),
                brandResult.updatedAt()
        );
    }
}
