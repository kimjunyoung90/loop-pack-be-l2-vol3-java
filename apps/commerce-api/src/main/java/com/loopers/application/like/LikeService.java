package com.loopers.application.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
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

    private final LikeRepository likeRepository;

    @Transactional
    public LikeInfo createLike(Long userId, Long productId) {
        likeRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(like -> {
                    throw new CoreException(ErrorType.CONFLICT, "이미 좋아요한 상품입니다.");
                });

        Like newLike = new Like(userId, productId);
        return LikeInfo.from(likeRepository.save(newLike));
    }

    @Transactional(readOnly = true)
    public Page<LikeInfo> getLikes(Long userId, Pageable pageable) {
        return likeRepository.findAllByUserId(userId, pageable)
                .map(LikeInfo::from);
    }

    @Transactional
    public void deleteLike(Long userId, Long productId) {
        Like like = likeRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "좋아요를 찾을 수 없습니다."));
        likeRepository.delete(like);
    }

    @Transactional
    public void deleteLikesByProductId(Long productId) {
        likeRepository.deleteByProductId(productId);
    }
}
