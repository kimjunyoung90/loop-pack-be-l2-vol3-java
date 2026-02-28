package com.loopers.infrastructure.like;

import com.loopers.domain.like.ProductLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductLikeJpaRepository extends JpaRepository<ProductLike, Long> {

    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);

    Page<ProductLike> findAllByUserId(Long userId, Pageable pageable);

    void deleteByProductId(Long productId);
}
