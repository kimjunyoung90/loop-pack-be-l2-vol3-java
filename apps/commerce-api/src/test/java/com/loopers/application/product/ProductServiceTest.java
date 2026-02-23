package com.loopers.application.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void 상품을_생성하면_저장된_상품의_ProductInfo를_반환한다() {
        // given
        CreateProductCommand command = new CreateProductCommand(1L, "운동화", 100000, 50);
        Product product = Product.builder()
                .brandId(1L)
                .name("운동화")
                .price(100000)
                .stock(50)
                .build();
        given(productRepository.save(any(Product.class))).willReturn(product);

        // when
        ProductInfo result = productService.createProduct(command);

        // then
        assertThat(result.brandId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("운동화");
        assertThat(result.price()).isEqualTo(100000);
        assertThat(result.stock()).isEqualTo(50);
    }

    @Test
    void 상품_목록을_조회하면_삭제되지_않은_상품을_Page로_반환한다() {
        // given
        Product product = Product.builder()
                .brandId(1L)
                .name("운동화")
                .price(100000)
                .stock(50)
                .build();
        PageRequest pageable = PageRequest.of(0, 20);
        given(productRepository.findAllByDeletedAtIsNull(pageable)).willReturn(new PageImpl<>(List.of(product)));

        // when
        Page<ProductInfo> result = productService.getProducts(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("운동화");
    }

    @Test
    void 존재하는_상품을_조회하면_ProductInfo를_반환한다() {
        // given
        Product product = Product.builder()
                .brandId(1L)
                .name("운동화")
                .price(100000)
                .stock(50)
                .build();
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when
        ProductInfo result = productService.getProduct(1L);

        // then
        assertThat(result.name()).isEqualTo("운동화");
        assertThat(result.price()).isEqualTo(100000);
    }

    @Test
    void 존재하지_않는_상품을_조회하면_CoreException_NOT_FOUND가_발생한다() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProduct(1L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 존재하는_상품을_수정하면_변경된_정보가_반영된_ProductInfo를_반환한다() {
        // given
        Product product = Product.builder()
                .brandId(1L)
                .name("운동화")
                .price(100000)
                .stock(50)
                .build();
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        UpdateProductCommand command = new UpdateProductCommand(2L, "슬리퍼", 50000, 30);

        // when
        ProductInfo result = productService.updateProduct(1L, command);

        // then
        assertThat(result.brandId()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("슬리퍼");
        assertThat(result.price()).isEqualTo(50000);
        assertThat(result.stock()).isEqualTo(30);
    }

    @Test
    void 존재하지_않는_상품을_수정하면_CoreException_NOT_FOUND가_발생한다() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.empty());
        UpdateProductCommand command = new UpdateProductCommand(2L, "슬리퍼", 50000, 30);

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(1L, command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 존재하는_상품을_삭제하면_deletedAt이_설정된다() {
        // given
        Product product = Product.builder()
                .brandId(1L)
                .name("운동화")
                .price(100000)
                .stock(50)
                .build();
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when
        productService.deleteProduct(1L);

        // then
        assertThat(product.getDeletedAt()).isNotNull();
    }

    @Test
    void 존재하지_않는_상품을_삭제하면_CoreException_NOT_FOUND가_발생한다() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(CoreException.class);
    }
}