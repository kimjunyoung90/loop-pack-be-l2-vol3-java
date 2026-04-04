package com.loopers.domain.product;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductCacheRepository {

    // 단건
    Optional<Product> getProduct(Long productId);
    void putProduct(Long productId, Product product);
    void evictProduct(Long productId);
    List<Product> multiGetProducts(List<Long> productIds);

    // 목록 (ID 리스트 캐싱)
    Optional<CachedProductIds> getProductIds(Pageable pageable);
    Optional<CachedProductIds> getProductIds(Long brandId, Pageable pageable);
    void putProductIds(Pageable pageable, List<Long> productIds, long totalElements);
    void putProductIds(Long brandId, Pageable pageable, List<Long> productIds, long totalElements);

    // 전체 목록 캐시 무효화
    void evictAllProductsCache();
}
