package com.loopers.infrastructure.product;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.ProductWithLikeCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;


import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ProductCacheRepositoryImpl implements ProductCacheRepository {

    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String PRODUCTS_KEY_PREFIX = "products:";
    private static final Duration TTL = Duration.ofHours(1);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper cacheObjectMapper;

    public ProductCacheRepositoryImpl(
            @Qualifier("defaultRedisTemplate") RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.cacheObjectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.cacheObjectMapper.addMixIn(BaseEntity.class, EntityCacheMixin.class);
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class EntityCacheMixin {}

    // === 단건: Product ===

    @Override
    public Optional<Product> getProduct(Long productId) {
        return getFromCache(PRODUCT_KEY_PREFIX + productId, Product.class);
    }

    @Override
    public void putProduct(Long productId, Product product) {
        putToCache(PRODUCT_KEY_PREFIX + productId, product);
    }

    @Override
    public void evictProduct(Long productId) {
        try {
            redisTemplate.delete(PRODUCT_KEY_PREFIX + productId);
            redisTemplate.delete(PRODUCT_KEY_PREFIX + productId + ":like");
        } catch (Exception e) {
            log.warn("캐시 삭제 실패: productId={}", productId, e);
        }
    }

    // === 단건: ProductWithLikeCount ===

    @Override
    public Optional<ProductWithLikeCount> getProductWithLikeCount(Long productId) {
        return getFromCache(PRODUCT_KEY_PREFIX + productId + ":like", ProductWithLikeCount.class);
    }

    @Override
    public void putProductWithLikeCount(Long productId, ProductWithLikeCount productWithLikeCount) {
        putToCache(PRODUCT_KEY_PREFIX + productId + ":like", productWithLikeCount);
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
        putToCache(key, new CachedProductPage(products.getContent(), products.getTotalElements(), products.getNumber(), products.getSize()));
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
        putToCache(key, new CachedProductWithLikeCountPage(products.getContent(), products.getTotalElements(), products.getNumber(), products.getSize()));
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
        putToCache(key, new CachedProductWithLikeCountPage(products.getContent(), products.getTotalElements(), products.getNumber(), products.getSize()));
    }

    // === 전체 목록 캐시 무효화 ===

    @Override
    public void evictAllProductsCache() {
        try {
            Set<String> keys = redisTemplate.keys(PRODUCTS_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("목록 캐시 전체 삭제 실패", e);
        }
    }

    // === private helpers ===

    private <T> Optional<T> getFromCache(String key, Class<T> type) {
		try {
			String json = redisTemplate.opsForValue().get(key);
			if (json == null) {
				return Optional.empty();
			}
			return Optional.of(cacheObjectMapper.readValue(json, type));
		} catch (JsonProcessingException e) {
			log.warn("캐시 역직렬화 실패: key={}", key, e);
			try {
				redisTemplate.delete(key);
			} catch (Exception ignored) {
				log.warn("캐시 삭제 실패: key={}", key, e);
			}
			return Optional.empty();
		} catch (Exception e) {
			log.warn("캐시 조회 실패: key={}", key, e);
			return Optional.empty();
		}
    }

    private void putToCache(String key, Object value) {
        try {
            String json = cacheObjectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, TTL);
        } catch (Exception e) {
            log.warn("캐시 저장 실패: key={}", key, e);
        }
    }

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
