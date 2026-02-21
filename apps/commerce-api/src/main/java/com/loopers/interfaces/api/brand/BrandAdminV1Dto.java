package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandInfo;
import jakarta.validation.constraints.NotBlank;

import java.time.ZonedDateTime;

public class BrandAdminV1Dto {

    public record CreateBrandRequest(
            @NotBlank
            String name
    ) {
    }

    public record CreateBrandResponse(
            Long id,
            String name,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static CreateBrandResponse from(BrandInfo brandInfo) {
            return new CreateBrandResponse(
                    brandInfo.id(),
                    brandInfo.name(),
                    brandInfo.createdAt(),
                    brandInfo.updatedAt()
            );
        }
    }

    public record GetBrandResponse(
            Long id,
            String name,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static GetBrandResponse from(BrandInfo brandInfo) {
            return new GetBrandResponse(
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

    public record UpdateBrandResponse(
            Long id,
            String name,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static UpdateBrandResponse from(BrandInfo brandInfo) {
            return new UpdateBrandResponse(
                    brandInfo.id(),
                    brandInfo.name(),
                    brandInfo.createdAt(),
                    brandInfo.updatedAt()
            );
        }
    }
}
