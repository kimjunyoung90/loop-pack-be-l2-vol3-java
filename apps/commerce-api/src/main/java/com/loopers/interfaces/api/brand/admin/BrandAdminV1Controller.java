package com.loopers.interfaces.api.brand.admin;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.command.BrandCreateCommand;
import com.loopers.application.brand.command.BrandUpdateCommand;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.brand.admin.request.BrandCreateRequest;
import com.loopers.interfaces.api.brand.admin.request.BrandUpdateRequest;
import com.loopers.interfaces.api.brand.admin.response.BrandCreateResponse;
import com.loopers.interfaces.api.brand.admin.response.BrandDetailResponse;
import com.loopers.interfaces.api.brand.admin.response.BrandUpdateResponse;
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
    public ApiResponse<BrandCreateResponse> registerBrand(
            @Valid @RequestBody BrandCreateRequest request
    ) {
        BrandResult brandResult = brandService.registerBrand(new BrandCreateCommand(request.name()));
        return ApiResponse.success(BrandCreateResponse.from(brandResult));
    }

    @GetMapping
    @Override
    public ApiResponse<Page<BrandDetailResponse>> getBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<BrandDetailResponse> brands = brandService.getBrands(PageRequest.of(page, size))
                .map(BrandDetailResponse::from);
        return ApiResponse.success(brands);
    }

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandDetailResponse> getBrand(
            @PathVariable Long brandId
    ) {
        BrandResult brandResult = brandService.getBrand(brandId);
        return ApiResponse.success(BrandDetailResponse.from(brandResult));
    }

    @PutMapping("/{brandId}")
    @Override
    public ApiResponse<BrandUpdateResponse> modifyBrand(
            @PathVariable Long brandId,
            @Valid @RequestBody BrandUpdateRequest request
    ) {
        BrandResult brandResult = brandService.modifyBrand(brandId, new BrandUpdateCommand(request.name()));
        return ApiResponse.success(BrandUpdateResponse.from(brandResult));
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
