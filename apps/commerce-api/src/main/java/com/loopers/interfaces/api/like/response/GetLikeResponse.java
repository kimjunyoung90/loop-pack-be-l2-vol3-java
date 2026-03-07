package com.loopers.interfaces.api.like.response;

import com.loopers.application.like.result.LikeResult;

import java.time.ZonedDateTime;

public record GetLikeResponse(
        Long id,
        Long userId,
        Long productId,
        ZonedDateTime createdAt
) {
    public static GetLikeResponse from(LikeResult result) {
        return new GetLikeResponse(
                result.id(),
                result.userId(),
                result.productId(),
                result.createdAt()
        );
    }
}
