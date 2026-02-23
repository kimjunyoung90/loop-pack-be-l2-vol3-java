package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandInfo;
import com.loopers.application.brand.BrandService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/brands")
public class BrandV1Controller implements BrandV1ApiSpec {

    private final BrandService brandService;

    @GetMapping
    @Override
    public ApiResponse<Page<BrandV1Dto.GetBrandResponse>> getBrands(
            @RequestParam int page,
            @RequestParam int size
    ) {
        Page<BrandInfo> brandInfo = brandService.getBrands(PageRequest.of(page, size));
        return ApiResponse.success(brandInfo.map(BrandV1Dto.GetBrandResponse::from));
    }

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandV1Dto.GetBrandResponse> getBrand(@PathVariable Long brandId) {
        BrandInfo brandInfo = brandService.getBrand(brandId);
        return ApiResponse.success(BrandV1Dto.GetBrandResponse.from(brandInfo));
    }
}
