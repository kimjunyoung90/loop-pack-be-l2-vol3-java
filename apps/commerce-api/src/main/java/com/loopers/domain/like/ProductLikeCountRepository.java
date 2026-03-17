package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface ProductLikeCountRepository {

    Optional<ProductLikeCount> findByProductId(Long productId);

    List<ProductLikeCount> findByProductIdIn(List<Long> productIds);
}
