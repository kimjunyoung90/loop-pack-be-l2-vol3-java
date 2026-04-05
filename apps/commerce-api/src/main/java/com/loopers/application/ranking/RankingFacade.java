package com.loopers.application.ranking;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.result.ProductResult;
import com.loopers.application.product.result.ProductWithBrandResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        List<ProductWithBrandResult> results = rankedProductIds.stream()
                .map(productId -> {
                    ProductResult product = productService.getProduct(productId);
                    BrandResult brand = brandService.getBrand(product.brandId());
                    return ProductWithBrandResult.from(product, brand.name());
                })
                .toList();

        return new PageImpl<>(results, pageable, totalCount);
    }
}
