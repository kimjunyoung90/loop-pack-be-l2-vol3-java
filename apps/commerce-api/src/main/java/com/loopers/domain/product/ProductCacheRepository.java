package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductCacheRepository {

    // 단건
    Optional<Product> getProduct(Long productId);
    void putProduct(Long productId, Product product);
    void evictProduct(Long productId);

    Optional<ProductWithLikeCount> getProductWithLikeCount(Long productId);
    void putProductWithLikeCount(Long productId, ProductWithLikeCount productWithLikeCount);

    // 목록
    Optional<Page<Product>> getProducts(Pageable pageable);
    void putProducts(Pageable pageable, Page<Product> products);

    Optional<Page<ProductWithLikeCount>> getProductsWithLikeCount(Pageable pageable);
    void putProductsWithLikeCount(Pageable pageable, Page<ProductWithLikeCount> products);

    Optional<Page<ProductWithLikeCount>> getProductsWithLikeCount(Long brandId, Pageable pageable);
    void putProductsWithLikeCount(Long brandId, Pageable pageable, Page<ProductWithLikeCount> products);

    // 전체 목록 캐시 무효화
    void evictAllProductsCache();
}
