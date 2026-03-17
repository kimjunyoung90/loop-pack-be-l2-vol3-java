package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Page<Product> findAllByDeletedAtIsNull(Pageable pageable);

    List<Product> findAllByBrandId(Long brandId);

    List<Product> findAllByBrandIdAndDeletedAtIsNull(Long brandId);

    Optional<Product> findById(Long productId);

    Optional<Product> findByIdAndDeletedAtIsNull(Long productId);

    Optional<Product> findByIdWithLockAndDeletedAtIsNull(Long productId);

    Product save(Product product);

    Optional<ProductWithLikeCount> findWithLikeCountByIdAndDeletedAtIsNull(Long productId);

    Page<ProductWithLikeCount> findAllWithLikeCountByDeletedAtIsNull(Pageable pageable);

    Page<ProductWithLikeCount> findAllWithLikeCountByBrandIdAndDeletedAtIsNull(Long brandId, Pageable pageable);
}
