package com.loopers.interfaces.api.brand.admin;

import com.loopers.application.brand.BrandInfo;
import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.CreateBrandCommand;
import com.loopers.application.brand.UpdateBrandCommand;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/brands")
public class BrandAdminV1Controller implements BrandAdminV1ApiSpec {

    private static final String ADMIN_LDAP = "loopers.admin";
    private static final String LDAP_HEADER = "X-Loopers-Ldap";

    private final BrandService brandService;

    @PostMapping
    @Override
    public ApiResponse<BrandAdminV1Dto.CreateBrandResponse> createBrand(
            @RequestHeader(value = LDAP_HEADER, required = false) String ldap,
            @Valid @RequestBody BrandAdminV1Dto.CreateBrandRequest request
    ) {
        validateAdmin(ldap);
        BrandInfo brandInfo = brandService.createBrand(new CreateBrandCommand(request.name()));
        return ApiResponse.success(BrandAdminV1Dto.CreateBrandResponse.from(brandInfo));
    }

    @GetMapping
    @Override
    public ApiResponse<Page<BrandAdminV1Dto.GetBrandResponse>> getBrands(
            @RequestHeader(value = LDAP_HEADER, required = false) String ldap,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validateAdmin(ldap);
        Page<BrandAdminV1Dto.GetBrandResponse> brands = brandService.getBrands(PageRequest.of(page, size))
                .map(BrandAdminV1Dto.GetBrandResponse::from);
        return ApiResponse.success(brands);
    }

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandAdminV1Dto.GetBrandResponse> getBrand(
            @RequestHeader(value = LDAP_HEADER, required = false) String ldap,
            @PathVariable Long brandId
    ) {
        validateAdmin(ldap);
        BrandInfo brandInfo = brandService.getBrand(brandId);
        return ApiResponse.success(BrandAdminV1Dto.GetBrandResponse.from(brandInfo));
    }

    @PutMapping("/{brandId}")
    @Override
    public ApiResponse<BrandAdminV1Dto.UpdateBrandResponse> updateBrand(
            @RequestHeader(value = LDAP_HEADER, required = false) String ldap,
            @PathVariable Long brandId,
            @Valid @RequestBody BrandAdminV1Dto.UpdateBrandRequest request
    ) {
        validateAdmin(ldap);
        BrandInfo brandInfo = brandService.updateBrand(brandId, new UpdateBrandCommand(request.name()));
        return ApiResponse.success(BrandAdminV1Dto.UpdateBrandResponse.from(brandInfo));
    }

    @DeleteMapping("/{brandId}")
    @Override
    public ApiResponse<Object> deleteBrand(
            @RequestHeader(value = LDAP_HEADER, required = false) String ldap,
            @PathVariable Long brandId
    ) {
        validateAdmin(ldap);
        brandService.deleteBrand(brandId);
        return ApiResponse.success();
    }

    private void validateAdmin(String ldap) {
        if (!ADMIN_LDAP.equals(ldap)) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }
    }
}
