package com.loopers.interfaces.api.brand.admin;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.brand.admin.request.BrandCreateRequest;
import com.loopers.interfaces.api.brand.admin.request.BrandUpdateRequest;
import com.loopers.interfaces.api.brand.admin.response.BrandCreateResponse;
import com.loopers.interfaces.api.brand.admin.response.BrandDetailResponse;
import com.loopers.interfaces.api.brand.admin.response.BrandUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

@Tag(name = "Brand Admin V1 API", description = "브랜드 관련 관리자 API 입니다.")
public interface BrandAdminV1ApiSpec {

    @Operation(
        summary = "브랜드 등록",
        description = "새로운 브랜드를 등록합니다."
    )
    ApiResponse<BrandCreateResponse> registerBrand(BrandCreateRequest request);

    @Operation(
        summary = "브랜드 목록 조회",
        description = "브랜드 목록을 페이징하여 조회합니다."
    )
    ApiResponse<PageResponse<BrandDetailResponse>> getBrands(Pageable pageable);

    @Operation(
        summary = "브랜드 상세 조회",
        description = "브랜드 상세 정보를 조회합니다."
    )
    ApiResponse<BrandDetailResponse> getBrand(Long brandId);

    @Operation(
        summary = "브랜드 수정",
        description = "브랜드 정보를 수정합니다."
    )
    ApiResponse<BrandUpdateResponse> modifyBrand(Long brandId, BrandUpdateRequest request);

    @Operation(
        summary = "브랜드 삭제",
        description = "브랜드를 삭제합니다."
    )
    ApiResponse<Object> deleteBrand(Long brandId);
}
