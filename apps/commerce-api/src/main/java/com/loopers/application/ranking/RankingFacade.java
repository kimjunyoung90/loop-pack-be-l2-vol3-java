package com.loopers.application.ranking;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.result.ProductResult;
import com.loopers.application.product.result.ProductWithBrandResult;
import com.loopers.application.ranking.result.ProductRankResult;
import com.loopers.domain.ranking.RankingPeriod;
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
    public Page<ProductWithBrandResult> getRankedProducts(RankingPeriod period, String date, Pageable pageable) {
        return switch (period) {
            case DAILY -> getDailyRankedProducts(date, pageable);
            case WEEKLY -> getWeeklyRankedProducts(date, pageable);
            case MONTHLY -> getMonthlyRankedProducts(date, pageable);
        };
    }

    private Page<ProductWithBrandResult> getDailyRankedProducts(String date, Pageable pageable) {
        long offset = pageable.getOffset();
        int size = pageable.getPageSize();

        List<Long> rankedProductIds = rankingService.getRankedProductIds(date, offset, size);
        if (rankedProductIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        long totalCount = rankingService.getTotalCount(date);

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

    private Page<ProductWithBrandResult> getWeeklyRankedProducts(String date, Pageable pageable) {
        Page<ProductRankResult> ranks = rankingService.getWeeklyRanks(date, pageable);
        return enrichWithProductInfo(ranks);
    }

    private Page<ProductWithBrandResult> getMonthlyRankedProducts(String date, Pageable pageable) {
        Page<ProductRankResult> ranks = rankingService.getMonthlyRanks(date, pageable);
        return enrichWithProductInfo(ranks);
    }

    private Page<ProductWithBrandResult> enrichWithProductInfo(Page<ProductRankResult> ranks) {
        List<ProductWithBrandResult> results = ranks.getContent().stream()
                .map(rank -> {
                    ProductResult product = productService.getProduct(rank.productId());
                    BrandResult brand = brandService.getBrand(product.brandId());
                    return ProductWithBrandResult.from(product, brand.name(), (long) rank.rankNumber());
                })
                .toList();

        return new PageImpl<>(results, ranks.getPageable(), ranks.getTotalElements());
    }
}
