package com.loopers.interfaces.api.ranking;

import com.loopers.application.product.result.ProductWithBrandResult;
import com.loopers.application.ranking.RankingFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.product.response.ProductWithBrandDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/rankings")
public class RankingV1Controller implements RankingV1ApiSpec {

    private final RankingFacade rankingFacade;

    @GetMapping
    @Override
    public ApiResponse<PageResponse<ProductWithBrandDetailResponse>> getRankings(
            @RequestParam(required = false) String date,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductWithBrandDetailResponse> rankings = rankingFacade.getRankedProducts(date, pageable)
                .map(ProductWithBrandDetailResponse::from);
        return ApiResponse.success(PageResponse.from(rankings));
    }
}
