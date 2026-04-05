package com.loopers.application.ranking;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.result.ProductResult;
import com.loopers.application.product.result.ProductWithBrandResult;
import com.loopers.domain.ranking.RankingCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class RankingFacade {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RankingCacheRepository rankingCacheRepository;
    private final ProductService productService;
    private final BrandService brandService;

    @Transactional(readOnly = true)
    public Page<ProductWithBrandResult> getRankedProducts(String date, Pageable pageable) {
        String resolvedDate = (date != null) ? date : LocalDate.now().format(DATE_FORMATTER);
        long offset = pageable.getOffset();
        int size = pageable.getPageSize();

        List<Long> rankedProductIds = rankingCacheRepository.getTopRankedProductIds(resolvedDate, offset, size);
        if (rankedProductIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        long totalCount = rankingCacheRepository.getTotalCount(resolvedDate);

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
