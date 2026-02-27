package com.loopers.application.like;

import com.loopers.domain.like.ProductLike;
import com.loopers.domain.like.ProductLikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Transactional(readOnly = true)
    public Page<LikeInfo> getLikes(Long userId, Pageable pageable) {
        return productLikeRepository.findAllByUserId(userId, pageable)
                .map(LikeInfo::from);
    }

    @Transactional
    public void deleteLike(Long userId, Long productId) {
        ProductLike productLike = productLikeRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "좋아요를 찾을 수 없습니다."));
        productLikeRepository.delete(productLike);
    }

    @Transactional
    public void deleteLikesByProductId(Long productId) {
        productLikeRepository.deleteByProductId(productId);
    }
}
