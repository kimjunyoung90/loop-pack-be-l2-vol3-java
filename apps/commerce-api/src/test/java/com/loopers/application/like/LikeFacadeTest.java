package com.loopers.application.like;

import com.loopers.application.like.result.LikeResult;
import com.loopers.application.product.ProductService;
import com.loopers.domain.product.Product;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
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
    void 존재하는_상품에_좋아요를_등록하면_상품_검증_후_LikeResult를_반환한다() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        Product product = Product.builder()
                .brandId(1L)
                .name("운동화")
                .price(100000)
                .stock(50)
                .build();
        ZonedDateTime now = ZonedDateTime.now();
        LikeResult expectedResult = new LikeResult(1L, userId, productId, now);

        given(productService.findProduct(productId)).willReturn(product);
        given(likeService.like(userId, productId)).willReturn(expectedResult);

        // when
        LikeResult result = likeFacade.like(userId, productId);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.productId()).isEqualTo(productId);
        verify(productService).incrementLikeCount(productId);
    }

    @Test
    void 존재하지_않는_상품에_좋아요를_등록하면_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long productId = 999L;
        willThrow(new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."))
                .given(productService).findProduct(productId);

        // when & then
        assertThatThrownBy(() -> likeFacade.like(userId, productId))
                .isInstanceOf(CoreException.class);
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