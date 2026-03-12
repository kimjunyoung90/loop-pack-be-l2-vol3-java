package com.loopers.infrastructure.like;

import com.loopers.domain.like.ProductLikeCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductLikeCountJpaRepository extends JpaRepository<ProductLikeCount, Long> {

    Optional<ProductLikeCount> findByProductId(Long productId);

    List<ProductLikeCount> findByProductIdIn(List<Long> productIds);
}
