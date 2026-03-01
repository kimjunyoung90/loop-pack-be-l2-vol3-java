package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandInfo;

public class BrandV1Dto {

    public record GetBrandResponse(
            Long id,
            String name
    ) {
        public static GetBrandResponse from(BrandInfo brandInfo) {
            return new GetBrandResponse(
                    brandInfo.id(),
                    brandInfo.name()
            );
        }
    }
}
