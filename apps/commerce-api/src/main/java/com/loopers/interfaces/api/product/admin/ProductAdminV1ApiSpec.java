package com.loopers.interfaces.api.product.admin;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.product.admin.request.ProductCreateRequest;
import com.loopers.interfaces.api.product.admin.request.ProductUpdateRequest;
import com.loopers.interfaces.api.product.admin.response.ProductCreateResponse;
import com.loopers.interfaces.api.product.admin.response.ProductDetailResponse;
import com.loopers.interfaces.api.product.admin.response.ProductUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

@Tag(name = "Product Admin V1 API", description = "상품 관련 관리자 API 입니다.")
public interface ProductAdminV1ApiSpec {

    @Operation(
        summary = "상품 등록",
        description = "새로운 상품을 등록합니다."
    )
    ApiResponse<ProductCreateResponse> registerProduct(ProductCreateRequest request);

    @Operation(
        summary = "상품 목록 조회",
        description = "상품 목록을 페이징하여 조회합니다."
    )
    ApiResponse<PageResponse<ProductDetailResponse>> getProducts(Pageable pageable);

    @Operation(
        summary = "상품 상세 조회",
        description = "상품 상세 정보를 조회합니다."
    )
    ApiResponse<ProductDetailResponse> getProduct(Long productId);

    @Operation(
        summary = "상품 수정",
        description = "상품 정보를 수정합니다."
    )
    ApiResponse<ProductUpdateResponse> modifyProduct(Long productId, ProductUpdateRequest request);

    @Operation(
        summary = "상품 삭제",
        description = "상품을 삭제합니다."
    )
    ApiResponse<Object> deleteProduct(Long productId);
}
