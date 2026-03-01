package com.loopers.domain.like;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductLikeRepository {

    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);

    Page<ProductLike> findAllByUserId(Long userId, Pageable pageable);

    ProductLike save(ProductLike productLike);

    void delete(ProductLike productLike);

    void deleteByProductId(Long productId);
}
