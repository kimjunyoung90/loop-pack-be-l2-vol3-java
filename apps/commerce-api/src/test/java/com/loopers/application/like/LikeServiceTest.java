package com.loopers.application.like;

import com.loopers.domain.like.ProductLike;
import com.loopers.domain.like.ProductLikeRepository;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private ProductLikeRepository productLikeRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    void 좋아요를_등록하면_저장된_LikeInfo를_반환한다() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        ProductLike productLike = new ProductLike(userId, productId);

        given(productLikeRepository.findByUserIdAndProductId(userId, productId)).willReturn(Optional.empty());
        given(productLikeRepository.save(any(ProductLike.class))).willReturn(productLike);

        // when
        LikeInfo result = likeService.createLike(userId, productId);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.productId()).isEqualTo(productId);
    }

    @Test
    void 이미_좋아요한_상품에_다시_좋아요하면_CoreException_CONFLICT가_발생한다() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        ProductLike existingLike = new ProductLike(userId, productId);

        given(productLikeRepository.findByUserIdAndProductId(userId, productId))
                .willReturn(Optional.of(existingLike));

        // when & then
        assertThatThrownBy(() -> likeService.createLike(userId, productId))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 좋아요를_취소하면_delete가_호출된다() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        ProductLike productLike = new ProductLike(userId, productId);

        given(productLikeRepository.findByUserIdAndProductId(userId, productId))
                .willReturn(Optional.of(productLike));

        // when
        likeService.deleteLike(userId, productId);

        // then
        then(productLikeRepository).should().delete(productLike);
    }

    @Test
    void 좋아요하지_않은_상품의_좋아요를_취소하면_CoreException_NOT_FOUND가_발생한다() {
        // given
        Long userId = 1L;
        Long productId = 999L;

        given(productLikeRepository.findByUserIdAndProductId(userId, productId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likeService.deleteLike(userId, productId))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 좋아요한_상품이_존재하면_해당_유저의_좋아요_목록을_Page로_반환한다() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        ProductLike productLike = new ProductLike(userId, productId);
        Page<ProductLike> productLikes = new PageImpl<>(List.of(productLike), pageable, 1);

        given(productLikeRepository.findAllByUserId(userId, pageable)).willReturn(productLikes);

        // when
        Page<LikeInfo> result = likeService.getLikes(userId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).userId()).isEqualTo(userId);
    }

    @Test
    void 좋아요한_상품이_없으면_빈_Page를_반환한다() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        Page<ProductLike> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        given(productLikeRepository.findAllByUserId(userId, pageable)).willReturn(emptyPage);

        // when
        Page<LikeInfo> result = likeService.getLikes(userId, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
