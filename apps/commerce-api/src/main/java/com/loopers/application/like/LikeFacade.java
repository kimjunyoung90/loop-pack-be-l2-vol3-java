package com.loopers.application.like;

import com.loopers.application.like.result.LikeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class LikeFacade {

    private final LikeService likeService;

    @Transactional
    public LikeResult like(Long userId, Long productId) {
        return likeService.like(userId, productId);
    }

    @Transactional
    public void unlike(Long userId, Long productId) {
        likeService.unlike(userId, productId);
    }
}
