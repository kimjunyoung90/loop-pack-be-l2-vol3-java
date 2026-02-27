package com.loopers.application.like;

import com.loopers.domain.like.ProductLike;
import com.loopers.domain.like.ProductLikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final ProductLikeRepository productLikeRepository;

    @Transactional
    public LikeInfo createLike(Long userId, Product product) {
        productLikeRepository.findByUserIdAndProductId(userId, product.getId())
                .ifPresent(like -> {
                    throw new CoreException(ErrorType.CONFLICT, "이미 좋아요한 상품입니다.");
                });

        ProductLike productLike = new ProductLike(userId, product);
        return LikeInfo.from(productLikeRepository.save(productLike));
    }
}
