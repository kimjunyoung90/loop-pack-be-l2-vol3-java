package com.loopers.infrastructure.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.ProductWithLikeCount;
import com.loopers.infrastructure.cache.RedisCacheRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductCacheRepositoryImpl extends RedisCacheRepository implements ProductCacheRepository {

    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String PRODUCTS_KEY_PREFIX = "products:";
    private static final Duration TTL = Duration.ofHours(1);

    public ProductCacheRepositoryImpl(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    // === 단건: Product ===

    @Override
    public Optional<Product> getProduct(Long productId) {
        return getFromCache(PRODUCT_KEY_PREFIX + productId, Product.class);
    }

    @Override
    public void putProduct(Long productId, Product product) {
        putToCache(PRODUCT_KEY_PREFIX + productId, product, TTL);
    }

    @Override
    public void evictProduct(Long productId) {
        safeDelete(PRODUCT_KEY_PREFIX + productId);
        safeDelete(PRODUCT_KEY_PREFIX + productId + ":like");
    }

    // === 단건: ProductWithLikeCount ===

    @Override
    public Optional<ProductWithLikeCount> getProductWithLikeCount(Long productId) {
        return getFromCache(PRODUCT_KEY_PREFIX + productId + ":like", ProductWithLikeCount.class);
    }

    @Override
    public void putProductWithLikeCount(Long productId, ProductWithLikeCount productWithLikeCount) {
        putToCache(PRODUCT_KEY_PREFIX + productId + ":like", productWithLikeCount, TTL);
    }

    // === 목록: Page<Product> ===

    @Override
    public Optional<Page<Product>> getProducts(Pageable pageable) {
        String key = PRODUCTS_KEY_PREFIX + buildPageSuffix(pageable);
        return getFromCache(key, CachedProductPage.class)
                .map(cached -> new PageImpl<>(cached.content, PageRequest.of(cached.page, cached.size), cached.totalElements));
    }

    @Override
    public void putProducts(Pageable pageable, Page<Product> products) {
        String key = PRODUCTS_KEY_PREFIX + buildPageSuffix(pageable);
        putToCache(key, new CachedProductPage(products.getContent(), products.getTotalElements(), products.getNumber(), products.getSize()), TTL);
    }

    // === 목록: Page<ProductWithLikeCount> ===

    @Override
    public Optional<Page<ProductWithLikeCount>> getProductsWithLikeCount(Pageable pageable) {
        String key = PRODUCTS_KEY_PREFIX + "like:" + buildPageSuffix(pageable);
        return getFromCache(key, CachedProductWithLikeCountPage.class)
                .map(cached -> new PageImpl<>(cached.content, PageRequest.of(cached.page, cached.size), cached.totalElements));
    }

    @Override
    public void putProductsWithLikeCount(Pageable pageable, Page<ProductWithLikeCount> products) {
        String key = PRODUCTS_KEY_PREFIX + "like:" + buildPageSuffix(pageable);
        putToCache(key, new CachedProductWithLikeCountPage(products.getContent(), products.getTotalElements(), products.getNumber(), products.getSize()), TTL);
    }

    @Override
    public Optional<Page<ProductWithLikeCount>> getProductsWithLikeCount(Long brandId, Pageable pageable) {
        String key = PRODUCTS_KEY_PREFIX + "like:brand:" + brandId + ":" + buildPageSuffix(pageable);
        return getFromCache(key, CachedProductWithLikeCountPage.class)
                .map(cached -> new PageImpl<>(cached.content, PageRequest.of(cached.page, cached.size), cached.totalElements));
    }

    @Override
    public void putProductsWithLikeCount(Long brandId, Pageable pageable, Page<ProductWithLikeCount> products) {
        String key = PRODUCTS_KEY_PREFIX + "like:brand:" + brandId + ":" + buildPageSuffix(pageable);
        putToCache(key, new CachedProductWithLikeCountPage(products.getContent(), products.getTotalElements(), products.getNumber(), products.getSize()), TTL);
    }

    // === 전체 목록 캐시 무효화 ===

    @Override
    public void evictAllProductsCache() {
        safeDeleteByPattern(PRODUCTS_KEY_PREFIX + "*");
    }

    // === private helpers ===

    private String buildPageSuffix(Pageable pageable) {
        String sort = pageable.getSort().stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .collect(Collectors.joining("_"));
        if (sort.isEmpty()) {
            sort = "unsorted";
        }
        return "page:" + pageable.getPageNumber() + ":size:" + pageable.getPageSize() + ":sort:" + sort;
    }

    // === 캐시 직렬화용 레코드 ===

    record CachedProductPage(List<Product> content, long totalElements, int page, int size) {}

    record CachedProductWithLikeCountPage(List<ProductWithLikeCount> content, long totalElements, int page, int size) {}
}
