package com.loopers.infrastructure.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.product.CachedProductIds;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.ProductWithLikeCount;
import com.loopers.infrastructure.cache.RedisCacheRepository;
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
    private static final Duration DETAIL_TTL = Duration.ofMinutes(5);
    private static final Duration LIST_TTL = Duration.ofMinutes(1);

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
        putToCache(PRODUCT_KEY_PREFIX + productId, product, DETAIL_TTL);
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
        putToCache(PRODUCT_KEY_PREFIX + productId + ":like", productWithLikeCount, DETAIL_TTL);
    }

    @Override
    public List<ProductWithLikeCount> multiGetProductsWithLikeCount(List<Long> productIds) {
        List<String> keys = productIds.stream()
                .map(id -> PRODUCT_KEY_PREFIX + id + ":like")
                .toList();
        return multiGetFromCache(keys, ProductWithLikeCount.class);
    }

    // === 목록: Product (Admin용) ===

    @Override
    public Optional<List<Product>> getProducts(Pageable pageable) {
        String key = PRODUCTS_KEY_PREFIX + "admin:" + buildPageSuffix(pageable);
        return getFromCache(key, CachedProductPage.class)
                .map(cached -> cached.content);
    }

    @Override
    public void putProducts(Pageable pageable, List<Product> products) {
        String key = PRODUCTS_KEY_PREFIX + "admin:" + buildPageSuffix(pageable);
        putToCache(key, new CachedProductPage(products), LIST_TTL);
    }

    record CachedProductPage(List<Product> content) {}

    // === 목록: ID 리스트 캐싱 ===

    @Override
    public Optional<CachedProductIds> getProductIds(Pageable pageable) {
        String key = PRODUCTS_KEY_PREFIX + buildPageSuffix(pageable);
        return getFromCache(key, CachedProductIds.class);
    }

    @Override
    public Optional<CachedProductIds> getProductIds(Long brandId, Pageable pageable) {
        String key = PRODUCTS_KEY_PREFIX + "brand:" + brandId + ":" + buildPageSuffix(pageable);
        return getFromCache(key, CachedProductIds.class);
    }

    @Override
    public void putProductIds(Pageable pageable, List<Long> productIds, long totalElements) {
        String key = PRODUCTS_KEY_PREFIX + buildPageSuffix(pageable);
        putToCache(key, new CachedProductIds(productIds, totalElements), LIST_TTL);
    }

    @Override
    public void putProductIds(Long brandId, Pageable pageable, List<Long> productIds, long totalElements) {
        String key = PRODUCTS_KEY_PREFIX + "brand:" + brandId + ":" + buildPageSuffix(pageable);
        putToCache(key, new CachedProductIds(productIds, totalElements), LIST_TTL);
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
}
