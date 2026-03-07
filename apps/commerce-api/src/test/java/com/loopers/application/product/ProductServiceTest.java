package com.loopers.application.product;

import com.loopers.application.product.command.ProductUpdateCommand;
import com.loopers.domain.product.ProductRepository;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void 존재하지_않는_상품_조회_시_예외가_발생한다() {
        // given
        given(productRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProduct(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }

    @Test
    void 존재하지_않는_상품_수정_시_예외가_발생한다() {
        // given
        given(productRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.empty());
        ProductUpdateCommand command = new ProductUpdateCommand(2L, "슬리퍼", 50000, 30);

        // when & then
        assertThatThrownBy(() -> productService.modifyProduct(1L, 2L, command))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }

    @Test
    void 존재하지_않는_상품_삭제_시_예외가_발생한다() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }
}
