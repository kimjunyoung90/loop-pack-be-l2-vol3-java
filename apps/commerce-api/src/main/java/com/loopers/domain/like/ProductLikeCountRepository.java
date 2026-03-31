package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface ProductLikeCountRepository {

    ProductLikeCount save(ProductLikeCount productLikeCount);

    Optional<ProductLikeCount> findByProductId(Long productId);

    List<ProductLikeCount> findByProductIdIn(List<Long> productIds);
}
