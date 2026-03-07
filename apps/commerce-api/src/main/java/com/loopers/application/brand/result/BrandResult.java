package com.loopers.application.brand.result;

import com.loopers.domain.brand.Brand;

import java.time.ZonedDateTime;

public record BrandResult(
        Long id,
        String name,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static BrandResult from(Brand brand) {
        return new BrandResult(
                brand.getId(),
                brand.getName(),
                brand.getCreatedAt(),
                brand.getUpdatedAt()
        );
    }
}
