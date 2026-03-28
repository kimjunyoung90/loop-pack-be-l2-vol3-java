package com.loopers.domain.brand;

import java.util.Optional;

public interface BrandCacheRepository {

    Optional<Brand> getBrand(Long brandId);

    void putBrand(Long brandId, Brand brand);

    void evictBrand(Long brandId);
}
