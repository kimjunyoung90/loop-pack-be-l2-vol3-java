package com.loopers.interfaces.api.brand.admin.response;

import com.loopers.application.brand.result.BrandResult;

import java.time.ZonedDateTime;

public record BrandDetailResponse(
        Long id,
        String name,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static BrandDetailResponse from(BrandResult brandResult) {
        return new BrandDetailResponse(
                brandResult.id(),
                brandResult.name(),
                brandResult.createdAt(),
                brandResult.updatedAt()
        );
    }
}
