package com.loopers.infrastructure.brand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandCacheRepository;
import com.loopers.infrastructure.cache.RedisCacheRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
public class BrandCacheRepositoryImpl extends RedisCacheRepository implements BrandCacheRepository {

    private static final String BRAND_KEY_PREFIX = "brand:";
    private static final Duration TTL = Duration.ofHours(1);

    public BrandCacheRepositoryImpl(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    @Override
    public Optional<Brand> getBrand(Long brandId) {
        return getFromCache(BRAND_KEY_PREFIX + brandId, Brand.class);
    }

    @Override
    public void putBrand(Long brandId, Brand brand) {
        putToCache(BRAND_KEY_PREFIX + brandId, brand, TTL);
    }

    @Override
    public void evictBrand(Long brandId) {
        safeDelete(BRAND_KEY_PREFIX + brandId);
    }
}
