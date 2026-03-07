package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.response.ProductDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;

@Tag(name = "Product V1 API", description = "상품 관련 API 입니다.")
public interface ProductV1ApiSpec {

    @Operation(
        summary = "상품 목록 조회",
        description = "상품 목록을 조회합니다. 정렬 조건(LATEST, PRICE_ASC, PRICE_DESC, LIKES_DESC)과 브랜드 필터를 지원합니다."
    )
    ApiResponse<Page<ProductDetailResponse>> getProducts(int page, int size, String sortBy, Long brandId);

    @Operation(
        summary = "상품 상세 조회",
        description = "상품 상세 정보를 조회합니다."
    )
    ApiResponse<ProductDetailResponse> getProduct(Long productId);
}
