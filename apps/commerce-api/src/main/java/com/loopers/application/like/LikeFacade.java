package com.loopers.application.like;

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
    public LikeInfo createLike(Long userId, Long productId) {
        productService.validateProductExists(productId);
        return likeService.createLike(userId, productId);
    }
}
