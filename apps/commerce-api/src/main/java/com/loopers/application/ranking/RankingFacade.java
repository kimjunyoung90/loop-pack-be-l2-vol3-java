package com.loopers.application.ranking;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.result.ProductResult;
import com.loopers.application.product.result.ProductWithBrandResult;
import com.loopers.application.ranking.result.ProductRankResult;
import com.loopers.application.ranking.result.ProductRankWithProductResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class RankingFacade {

    private final RankingService rankingService;
    private final ProductService productService;
    private final BrandService brandService;

    @Transactional(readOnly = true)
    public Page<ProductWithBrandResult> getRankedProducts(String date, Pageable pageable) {
        long offset = pageable.getOffset();
        int size = pageable.getPageSize();

		//1. 랭킹 상품 id 조회
        List<Long> rankedProductIds = rankingService.getRankedProductIds(date, offset, size);
        if (rankedProductIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

		//2. 랭킹 토탈 카운트 조회
        long totalCount = rankingService.getTotalCount(date);

		//3. 상품 정보, 브랜드 정보 조회(랭킹 순 반환)
        List<ProductWithBrandResult> results = new ArrayList<>();
        for (int i = 0; i < rankedProductIds.size(); i++) {
            Long productId = rankedProductIds.get(i);
            ProductResult product = productService.getProduct(productId);
            BrandResult brand = brandService.getBrand(product.brandId());
            long rank = offset + i + 1;
            results.add(ProductWithBrandResult.from(product, brand.name(), rank));
        }

        return new PageImpl<>(results, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public Page<ProductRankWithProductResult> getWeeklyRankedProducts(Pageable pageable) {
        Page<ProductRankResult> ranks = rankingService.getWeeklyRanks(pageable);
        return enrichWithProductInfo(ranks);
    }

    @Transactional(readOnly = true)
    public Page<ProductRankWithProductResult> getMonthlyRankedProducts(Pageable pageable) {
        Page<ProductRankResult> ranks = rankingService.getMonthlyRanks(pageable);
        return enrichWithProductInfo(ranks);
    }

    private Page<ProductRankWithProductResult> enrichWithProductInfo(Page<ProductRankResult> ranks) {
        List<ProductRankWithProductResult> results = ranks.getContent().stream()
                .map(rank -> {
                    ProductResult product = productService.getProduct(rank.productId());
                    BrandResult brand = brandService.getBrand(product.brandId());
                    return ProductRankWithProductResult.from(rank, product.name(), brand.name(), product.price());
                })
                .toList();

        return new PageImpl<>(results, ranks.getPageable(), ranks.getTotalElements());
    }
}
