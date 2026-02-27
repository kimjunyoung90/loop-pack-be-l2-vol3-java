package com.loopers.application.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.like.ProductLike;
import com.loopers.domain.like.ProductLikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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
        Brand brand = Brand.builder().name("나이키").build();
        Product product = Product.builder()
                .brand(brand)
                .name("운동화")
                .price(100000)
                .stock(50)
                .build();
        ProductLike productLike = new ProductLike(userId, product);

        given(productLikeRepository.findByUserIdAndProductId(userId, product.getId())).willReturn(Optional.empty());
        given(productLikeRepository.save(any(ProductLike.class))).willReturn(productLike);

        // when
        LikeInfo result = likeService.createLike(userId, product);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.productId()).isEqualTo(product.getId());
    }

    @Test
    void 이미_좋아요한_상품에_다시_좋아요하면_CoreException_CONFLICT가_발생한다() {
        // given
        Long userId = 1L;
        Brand brand = Brand.builder().name("나이키").build();
        Product product = Product.builder()
                .brand(brand)
                .name("운동화")
                .price(100000)
                .stock(50)
                .build();
        ProductLike existingLike = new ProductLike(userId, product);

        given(productLikeRepository.findByUserIdAndProductId(userId, product.getId()))
                .willReturn(Optional.of(existingLike));

        // when & then
        assertThatThrownBy(() -> likeService.createLike(userId, product))
                .isInstanceOf(CoreException.class);
    }
}
