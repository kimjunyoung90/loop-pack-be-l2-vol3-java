package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeInfo;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public class LikeV1Dto {

    public record CreateLikeRequest(
            @NotNull
            Long productId
    ) {
    }

    public record CreateLikeResponse(
            Long id,
            Long userId,
            Long productId,
            ZonedDateTime createdAt
    ) {
        public static CreateLikeResponse from(LikeInfo info) {
            return new CreateLikeResponse(
                    info.id(),
                    info.userId(),
                    info.productId(),
                    info.createdAt()
            );
        }
    }
}
