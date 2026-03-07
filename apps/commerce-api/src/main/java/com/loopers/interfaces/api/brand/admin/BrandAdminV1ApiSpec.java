package com.loopers.interfaces.api.brand.admin;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.brand.admin.request.CreateBrandRequest;
import com.loopers.interfaces.api.brand.admin.request.UpdateBrandRequest;
import com.loopers.interfaces.api.brand.admin.response.CreateBrandResponse;
import com.loopers.interfaces.api.brand.admin.response.GetBrandResponse;
import com.loopers.interfaces.api.brand.admin.response.UpdateBrandResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;

@Tag(name = "Brand Admin V1 API", description = "브랜드 관련 관리자 API 입니다.")
public interface BrandAdminV1ApiSpec {

    @Operation(
        summary = "브랜드 등록",
        description = "새로운 브랜드를 등록합니다."
    )
    ApiResponse<CreateBrandResponse> createBrand(CreateBrandRequest request);

    @Operation(
        summary = "브랜드 목록 조회",
        description = "브랜드 목록을 페이징하여 조회합니다."
    )
    ApiResponse<Page<GetBrandResponse>> getBrands(int page, int size);

    @Operation(
        summary = "브랜드 상세 조회",
        description = "브랜드 상세 정보를 조회합니다."
    )
    ApiResponse<GetBrandResponse> getBrand(Long brandId);

    @Operation(
        summary = "브랜드 수정",
        description = "브랜드 정보를 수정합니다."
    )
    ApiResponse<UpdateBrandResponse> updateBrand(Long brandId, UpdateBrandRequest request);

    @Operation(
        summary = "브랜드 삭제",
        description = "브랜드를 삭제합니다."
    )
    ApiResponse<Object> deleteBrand(Long brandId);
}
