package com.loopers.application.like;

import com.loopers.application.like.result.LikeResult;
import com.loopers.application.product.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LikeFacadeTest {

    @Mock
    private LikeService likeService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private LikeFacade likeFacade;

    @Test
    void 좋아요를_등록하면_LikeResult를_반환하고_좋아요수를_증가시킨다() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        ZonedDateTime now = ZonedDateTime.now();
        LikeResult expectedResult = new LikeResult(1L, userId, productId, now);

        given(likeService.like(userId, productId)).willReturn(expectedResult);

        // when
        LikeResult result = likeFacade.like(userId, productId);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.productId()).isEqualTo(productId);
        verify(productService).incrementLikeCount(productId);
    }

    @Test
    void 좋아요를_취소하면_좋아요_삭제_후_좋아요수를_감소시킨다() {
        // given
        Long userId = 1L;
        Long productId = 1L;

        // when
        likeFacade.unlike(userId, productId);

        // then
        verify(likeService).unlike(userId, productId);
        verify(productService).decrementLikeCount(productId);
    }
}