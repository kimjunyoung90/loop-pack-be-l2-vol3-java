package com.loopers.interfaces.api.brand.admin;

import com.loopers.application.brand.BrandInfo;
import jakarta.validation.constraints.NotBlank;

import java.time.ZonedDateTime;

public class BrandAdminV1Dto {

    public record CreateBrandRequest(
            @NotBlank
            String name
    ) {
    }

    public record BrandResponse(
            Long id,
            String name,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static BrandResponse from(BrandInfo brandInfo) {
            return new BrandResponse(
                    brandInfo.id(),
                    brandInfo.name(),
                    brandInfo.createdAt(),
                    brandInfo.updatedAt()
            );
        }
    }

    public record UpdateBrandRequest(
            @NotBlank
            String name
    ) {
    }
}
