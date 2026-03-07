package com.loopers.application.like;

import com.loopers.domain.like.ProductLike;
import com.loopers.domain.like.ProductLikeRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private ProductLikeRepository productLikeRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    void 이미_좋아요한_상품에_다시_좋아요하면_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        ProductLike existingLike = ProductLike.builder()
                .userId(userId)
                .productId(productId)
                .build();

        given(productLikeRepository.findByUserIdAndProductId(userId, productId))
                .willReturn(Optional.of(existingLike));

        // when & then
        assertThatThrownBy(() -> likeService.createLike(userId, productId))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.CONFLICT));
    }

    @Test
    void 좋아요하지_않은_상품의_좋아요를_취소하면_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long productId = 999L;

        given(productLikeRepository.findByUserIdAndProductId(userId, productId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likeService.deleteLike(userId, productId))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }
}
