package com.loopers.application.like;

import com.loopers.application.like.result.LikeResult;
import com.loopers.domain.like.ProductLike;
import com.loopers.domain.like.ProductLikeRepository;
import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final ProductLikeRepository productLikeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LikeResult like(Long userId, Long productId) {
        productLikeRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(like -> {
                    throw new CoreException(ErrorType.CONFLICT, "이미 좋아요한 상품입니다.");
                });

        ProductLike productLike = ProductLike.builder()
                .userId(userId)
                .productId(productId)
                .build();
        ProductLike saved = productLikeRepository.save(productLike);
        eventPublisher.publishEvent(new ProductLikedEvent(productId));
        return LikeResult.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<LikeResult> getLikes(Long userId, Pageable pageable) {
        return productLikeRepository.findAllByUserId(userId, pageable)
                .map(LikeResult::from);
    }

    @Transactional
    public void unlike(Long userId, Long productId) {
        ProductLike productLike = productLikeRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "좋아요를 찾을 수 없습니다."));
        productLikeRepository.delete(productLike);
        eventPublisher.publishEvent(new ProductUnlikedEvent(productId));
    }

    @Transactional
    public void deleteLikes(Long productId) {
        productLikeRepository.deleteByProductId(productId);
    }

}
