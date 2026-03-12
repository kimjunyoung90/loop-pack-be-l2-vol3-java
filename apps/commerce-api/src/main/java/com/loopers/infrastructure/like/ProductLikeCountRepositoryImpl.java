package com.loopers.infrastructure.like;

import com.loopers.domain.like.ProductLikeCount;
import com.loopers.domain.like.ProductLikeCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductLikeCountRepositoryImpl implements ProductLikeCountRepository {

    private final ProductLikeCountJpaRepository productLikeCountJpaRepository;

    @Override
    public Optional<ProductLikeCount> findByProductId(Long productId) {
        return productLikeCountJpaRepository.findByProductId(productId);
    }

    @Override
    public List<ProductLikeCount> findByProductIdIn(List<Long> productIds) {
        return productLikeCountJpaRepository.findByProductIdIn(productIds);
    }
}
