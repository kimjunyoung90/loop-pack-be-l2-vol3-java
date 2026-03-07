package com.loopers.application.like;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.CreateBrandCommand;
import com.loopers.application.brand.BrandInfo;
import com.loopers.application.product.CreateProductCommand;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.ProductInfo;
import com.loopers.support.error.CoreException;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
@Transactional
class LikeServiceIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    private ProductInfo createProduct() {
        BrandInfo brand = brandService.createBrand(new CreateBrandCommand("나이키"));
        return productService.createProduct(brand.id(),
                new CreateProductCommand(brand.id(), "운동화", 50000, 10));
    }

    @Test
    void 좋아요_등록_조회_취소_전체_흐름을_검증한다() {
        // given
        ProductInfo product = createProduct();
        Long userId = 1L;

        // 등록
        LikeInfo created = likeService.createLike(userId, product.id());
        assertThat(created.id()).isNotNull();
        assertThat(created.userId()).isEqualTo(userId);
        assertThat(created.productId()).isEqualTo(product.id());

        // 목록 조회
        Page<LikeInfo> likes = likeService.getLikes(userId, PageRequest.of(0, 20));
        assertThat(likes.getContent()).hasSize(1);
        assertThat(likes.getContent().getFirst().productId()).isEqualTo(product.id());

        // 취소
        likeService.deleteLike(userId, product.id());

        // 취소 후 목록 조회
        Page<LikeInfo> afterDelete = likeService.getLikes(userId, PageRequest.of(0, 20));
        assertThat(afterDelete.getContent()).isEmpty();
    }

    @Test
    void 좋아요가_없으면_빈_Page를_반환한다() {
        // when
        Page<LikeInfo> likes = likeService.getLikes(999L, PageRequest.of(0, 20));

        // then
        assertThat(likes.getContent()).isEmpty();
        assertThat(likes.getTotalElements()).isZero();
    }

    @Test
    void 이미_좋아요한_상품에_다시_좋아요하면_예외가_발생한다() {
        // given
        ProductInfo product = createProduct();
        Long userId = 1L;
        likeService.createLike(userId, product.id());

        // when & then
        assertThatThrownBy(() -> likeService.createLike(userId, product.id()))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 좋아요하지_않은_상품의_좋아요를_취소하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> likeService.deleteLike(1L, 999L))
                .isInstanceOf(CoreException.class);
    }
}
