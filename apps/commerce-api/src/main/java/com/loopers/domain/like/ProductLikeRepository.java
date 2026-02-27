package com.loopers.domain.like;

import java.util.Optional;

public interface ProductLikeRepository {

    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);

    ProductLike save(ProductLike productLike);
}
