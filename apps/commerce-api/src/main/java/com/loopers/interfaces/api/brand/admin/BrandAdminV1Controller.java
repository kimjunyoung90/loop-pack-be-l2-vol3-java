package com.loopers.interfaces.api.brand.admin;

import com.loopers.application.brand.BrandCommand;
import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandInfo;
import com.loopers.application.brand.BrandService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.auth.AdminOnly;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@AdminOnly
@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/brands")
public class BrandAdminV1Controller implements BrandAdminV1ApiSpec {

    private final BrandService brandService;
    private final BrandFacade brandFacade;

    @PostMapping
    @Override
    public ApiResponse<BrandAdminV1Dto.BrandResponse> createBrand(
            @Valid @RequestBody BrandAdminV1Dto.CreateBrandRequest request
    ) {
        BrandInfo brandInfo = brandService.createBrand(new BrandCommand.Create(request.name()));
        return ApiResponse.success(BrandAdminV1Dto.BrandResponse.from(brandInfo));
    }

    @GetMapping
    @Override
    public ApiResponse<Page<BrandAdminV1Dto.BrandResponse>> getBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<BrandAdminV1Dto.BrandResponse> brands = brandService.getBrands(PageRequest.of(page, size))
                .map(BrandAdminV1Dto.BrandResponse::from);
        return ApiResponse.success(brands);
    }

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandAdminV1Dto.BrandResponse> getBrand(
            @PathVariable Long brandId
    ) {
        BrandInfo brandInfo = brandService.getBrand(brandId);
        return ApiResponse.success(BrandAdminV1Dto.BrandResponse.from(brandInfo));
    }

    @PutMapping("/{brandId}")
    @Override
    public ApiResponse<BrandAdminV1Dto.BrandResponse> updateBrand(
            @PathVariable Long brandId,
            @Valid @RequestBody BrandAdminV1Dto.UpdateBrandRequest request
    ) {
        BrandInfo brandInfo = brandService.updateBrand(brandId, new BrandCommand.Update(request.name()));
        return ApiResponse.success(BrandAdminV1Dto.BrandResponse.from(brandInfo));
    }

    @DeleteMapping("/{brandId}")
    @Override
    public ApiResponse<Object> deleteBrand(
            @PathVariable Long brandId
    ) {
        brandFacade.deleteBrand(brandId);
        return ApiResponse.success();
    }
}
