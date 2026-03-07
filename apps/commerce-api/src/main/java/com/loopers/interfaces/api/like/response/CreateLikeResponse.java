package com.loopers.interfaces.api.like.response;

import com.loopers.application.like.result.LikeResult;

import java.time.ZonedDateTime;

public record CreateLikeResponse(
        Long id,
        Long userId,
        Long productId,
        ZonedDateTime createdAt
) {
    public static CreateLikeResponse from(LikeResult result) {
        return new CreateLikeResponse(
                result.id(),
                result.userId(),
                result.productId(),
                result.createdAt()
        );
    }
}
