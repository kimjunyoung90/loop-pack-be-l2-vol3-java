package com.loopers.interfaces.api.brand.admin;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.command.CreateBrandCommand;
import com.loopers.application.brand.command.UpdateBrandCommand;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.brand.admin.request.CreateBrandRequest;
import com.loopers.interfaces.api.brand.admin.request.UpdateBrandRequest;
import com.loopers.interfaces.api.brand.admin.response.CreateBrandResponse;
import com.loopers.interfaces.api.brand.admin.response.GetBrandResponse;
import com.loopers.interfaces.api.brand.admin.response.UpdateBrandResponse;
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
    public ApiResponse<CreateBrandResponse> createBrand(
            @Valid @RequestBody CreateBrandRequest request
    ) {
        BrandResult brandResult = brandService.createBrand(new CreateBrandCommand(request.name()));
        return ApiResponse.success(CreateBrandResponse.from(brandResult));
    }

    @GetMapping
    @Override
    public ApiResponse<Page<GetBrandResponse>> getBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<GetBrandResponse> brands = brandService.getBrands(PageRequest.of(page, size))
                .map(GetBrandResponse::from);
        return ApiResponse.success(brands);
    }

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<GetBrandResponse> getBrand(
            @PathVariable Long brandId
    ) {
        BrandResult brandResult = brandService.getBrand(brandId);
        return ApiResponse.success(GetBrandResponse.from(brandResult));
    }

    @PutMapping("/{brandId}")
    @Override
    public ApiResponse<UpdateBrandResponse> updateBrand(
            @PathVariable Long brandId,
            @Valid @RequestBody UpdateBrandRequest request
    ) {
        BrandResult brandResult = brandService.updateBrand(brandId, new UpdateBrandCommand(request.name()));
        return ApiResponse.success(UpdateBrandResponse.from(brandResult));
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
