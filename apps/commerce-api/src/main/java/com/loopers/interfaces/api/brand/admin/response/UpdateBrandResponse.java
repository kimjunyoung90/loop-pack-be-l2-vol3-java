package com.loopers.interfaces.api.brand.admin.response;

import com.loopers.application.brand.result.BrandResult;

import java.time.ZonedDateTime;

public record UpdateBrandResponse(
        Long id,
        String name,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static UpdateBrandResponse from(BrandResult brandResult) {
        return new UpdateBrandResponse(
                brandResult.id(),
                brandResult.name(),
                brandResult.createdAt(),
                brandResult.updatedAt()
        );
    }
}
