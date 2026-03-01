package com.loopers.infrastructure.like;

import com.loopers.domain.like.ProductLike;
import com.loopers.domain.like.ProductLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductLikeRepositoryImpl implements ProductLikeRepository {

    private final ProductLikeJpaRepository productLikeJpaRepository;

    @Override
    public Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId) {
        return productLikeJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public Page<ProductLike> findAllByUserId(Long userId, Pageable pageable) {
        return productLikeJpaRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public ProductLike save(ProductLike productLike) {
        return productLikeJpaRepository.save(productLike);
    }

    @Override
    public void delete(ProductLike productLike) {
        productLikeJpaRepository.delete(productLike);
    }

    @Override
    public void deleteByProductId(Long productId) {
        productLikeJpaRepository.deleteByProductId(productId);
    }
}
