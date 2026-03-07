package com.loopers.interfaces.api.like.response;

import com.loopers.application.like.result.LikeResult;

import java.time.ZonedDateTime;

public record LikeGetResponse(
        Long id,
        Long userId,
        Long productId,
        ZonedDateTime createdAt
) {
    public static LikeGetResponse from(LikeResult result) {
        return new LikeGetResponse(
                result.id(),
                result.userId(),
                result.productId(),
                result.createdAt()
        );
    }
}
