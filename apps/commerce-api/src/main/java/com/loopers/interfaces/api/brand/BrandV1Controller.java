package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.brand.response.BrandDetailResponse;
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
    public ApiResponse<Page<BrandDetailResponse>> getBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<BrandResult> brandResult = brandService.getBrands(PageRequest.of(page, size));
        return ApiResponse.success(brandResult.map(BrandDetailResponse::from));
    }

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandDetailResponse> getBrand(@PathVariable Long brandId) {
        BrandResult brandResult = brandService.getBrand(brandId);
        return ApiResponse.success(BrandDetailResponse.from(brandResult));
    }
}
