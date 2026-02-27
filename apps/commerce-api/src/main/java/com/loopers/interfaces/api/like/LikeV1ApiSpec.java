package com.loopers.interfaces.api.like;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Like V1 API", description = "좋아요 관련 API 입니다.")
public interface LikeV1ApiSpec {

    @Operation(
            summary = "좋아요 등록",
            description = "상품에 좋아요를 등록합니다."
    )
    ApiResponse<LikeV1Dto.CreateLikeResponse> createLike(
            com.loopers.support.auth.AuthUser authUser,
            Long productId
    );

    @Operation(
            summary = "좋아요 취소",
            description = "상품의 좋아요를 취소합니다."
    )
    ApiResponse<Void> deleteLike(
            com.loopers.support.auth.AuthUser authUser,
            Long productId
    );

    @Operation(
            summary = "좋아요한 상품 목록 조회",
            description = "좋아요한 상품 목록을 조회합니다."
    )
    ApiResponse<Page<LikeV1Dto.GetLikeResponse>> getLikes(
            com.loopers.support.auth.AuthUser authUser,
            Pageable pageable
    );
}
