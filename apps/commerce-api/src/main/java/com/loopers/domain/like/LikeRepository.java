package com.loopers.domain.like;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LikeRepository {

    Optional<Like> findByUserIdAndProductId(Long userId, Long productId);

    Page<Like> findAllByUserId(Long userId, Pageable pageable);

    Like save(Like like);

    void delete(Like like);

    void deleteByProductId(Long productId);
}
