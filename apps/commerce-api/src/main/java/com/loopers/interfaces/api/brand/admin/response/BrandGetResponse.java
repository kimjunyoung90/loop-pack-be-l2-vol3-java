package com.loopers.interfaces.api.brand.admin.response;

import com.loopers.application.brand.result.BrandResult;

import java.time.ZonedDateTime;

public record BrandGetResponse(
        Long id,
        String name,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static BrandGetResponse from(BrandResult brandResult) {
        return new BrandGetResponse(
                brandResult.id(),
                brandResult.name(),
                brandResult.createdAt(),
                brandResult.updatedAt()
        );
    }
}
