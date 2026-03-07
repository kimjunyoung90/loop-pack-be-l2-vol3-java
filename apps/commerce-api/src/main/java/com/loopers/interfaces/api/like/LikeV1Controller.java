package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.result.LikeResult;
import com.loopers.application.like.LikeService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.like.response.CreateLikeResponse;
import com.loopers.interfaces.api.like.response.GetLikeResponse;
import com.loopers.support.auth.AuthUser;
import com.loopers.support.auth.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/likes")
public class LikeV1Controller implements LikeV1ApiSpec {

    private final LikeFacade likeFacade;
    private final LikeService likeService;

    @PostMapping("/products/{productId}")
    @Override
    public ApiResponse<CreateLikeResponse> createLike(
            @LoginUser AuthUser authUser,
            @PathVariable Long productId
    ) {
        LikeResult likeResult = likeFacade.createLike(authUser.id(), productId);
        return ApiResponse.success(CreateLikeResponse.from(likeResult));
    }

    @DeleteMapping("/products/{productId}")
    @Override
    public ApiResponse<Void> deleteLike(
            @LoginUser AuthUser authUser,
            @PathVariable Long productId
    ) {
        likeService.deleteLike(authUser.id(), productId);
        return ApiResponse.success(null);
    }

    @GetMapping("/products")
    @Override
    public ApiResponse<Page<GetLikeResponse>> getLikes(
            @LoginUser AuthUser authUser,
            Pageable pageable
    ) {
        Page<LikeResult> likes = likeService.getLikes(authUser.id(), pageable);
        return ApiResponse.success(likes.map(GetLikeResponse::from));
    }
}
