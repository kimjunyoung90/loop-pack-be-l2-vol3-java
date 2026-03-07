package com.loopers.interfaces.api.like.response;

import com.loopers.application.like.result.LikeResult;

import java.time.ZonedDateTime;

public record LikeCreateResponse(
        Long id,
        Long userId,
        Long productId,
        ZonedDateTime createdAt
) {
    public static LikeCreateResponse from(LikeResult result) {
        return new LikeCreateResponse(
                result.id(),
                result.userId(),
                result.productId(),
                result.createdAt()
        );
    }
}
