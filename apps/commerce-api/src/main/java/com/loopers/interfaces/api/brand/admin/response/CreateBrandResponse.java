package com.loopers.interfaces.api.brand.admin.response;

import com.loopers.application.brand.result.BrandResult;

import java.time.ZonedDateTime;

public record CreateBrandResponse(
        Long id,
        String name,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static CreateBrandResponse from(BrandResult brandResult) {
        return new CreateBrandResponse(
                brandResult.id(),
                brandResult.name(),
                brandResult.createdAt(),
                brandResult.updatedAt()
        );
    }
}
