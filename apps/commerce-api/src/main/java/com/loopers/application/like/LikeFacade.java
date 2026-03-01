package com.loopers.application.like;

import com.loopers.application.product.ProductService;
import com.loopers.domain.product.Product;
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
        Product product = productService.findProduct(productId);
        return likeService.createLike(userId, product);
    }
}
