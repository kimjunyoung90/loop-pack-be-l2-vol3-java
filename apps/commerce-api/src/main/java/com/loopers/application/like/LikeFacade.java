package com.loopers.application.like;

import com.loopers.application.like.result.LikeResult;
import com.loopers.application.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class LikeFacade {

    private final LikeService likeService;
    private final ProductService productService;

    @Transactional
    public LikeResult like(Long userId, Long productId) {
        LikeResult result = likeService.like(userId, productId);
        productService.incrementLikeCount(productId);
        return result;
    }

    @Transactional
    public void unlike(Long userId, Long productId) {
        likeService.unlike(userId, productId);
        productService.decrementLikeCount(productId);
    }
}
