package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.brand.response.BrandDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/brands")
public class BrandV1Controller implements BrandV1ApiSpec {

    private final BrandService brandService;

    @GetMapping
    @Override
    public ApiResponse<PageResponse<BrandDetailResponse>> getBrands(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<BrandResult> brandResult = brandService.getBrands(pageable);
        return ApiResponse.success(PageResponse.from(brandResult.map(BrandDetailResponse::from)));
    }

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandDetailResponse> getBrand(@PathVariable Long brandId) {
        BrandResult brandResult = brandService.getBrand(brandId);
        return ApiResponse.success(BrandDetailResponse.from(brandResult));
    }
}
