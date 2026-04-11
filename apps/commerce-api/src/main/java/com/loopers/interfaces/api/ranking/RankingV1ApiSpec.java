package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.product.response.ProductWithBrandDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

@Tag(name = "Ranking V1 API", description = "상품 랭킹 관련 API 입니다.")
public interface RankingV1ApiSpec {

    @Operation(
            summary = "상품 랭킹 조회",
            description = "오늘의 인기 상품 랭킹을 조회합니다. date 파라미터로 특정 날짜의 랭킹을 조회할 수 있습니다."
    )
    ApiResponse<PageResponse<ProductWithBrandDetailResponse>> getRankings(String date, Pageable pageable);
}
