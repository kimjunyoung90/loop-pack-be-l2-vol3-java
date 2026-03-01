package com.loopers.application.product;

import com.loopers.application.brand.BrandService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class ProductFacadeTest {

    @Mock
    private ProductService productService;

    @Mock
    private BrandService brandService;

    @InjectMocks
    private ProductFacade productFacade;

    @Test
    void 존재하는_브랜드로_상품을_생성하면_브랜드_검증_후_ProductInfo를_반환한다() {
        // given
        ProductCommand.Create command = new ProductCommand.Create(1L, "운동화", 100000, 50);
        ZonedDateTime now = ZonedDateTime.now();
        ProductInfo expectedInfo = new ProductInfo(1L, 1L, "운동화", 100000, 50, now, now);
        given(productService.createProduct(eq(command))).willReturn(expectedInfo);

        // when
        ProductInfo result = productFacade.createProduct(command);

        // then
        assertThat(result.name()).isEqualTo("운동화");
        assertThat(result.price()).isEqualTo(100000);
    }

    @Test
    void 존재하지_않는_브랜드로_상품을_생성하면_CoreException_NOT_FOUND가_발생한다() {
        // given
        ProductCommand.Create command = new ProductCommand.Create(999L, "운동화", 100000, 50);
        willThrow(new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."))
                .given(brandService).validateBrandExists(999L);

        // when & then
        assertThatThrownBy(() -> productFacade.createProduct(command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 존재하는_브랜드로_상품을_수정하면_브랜드_검증_후_ProductInfo를_반환한다() {
        // given
        ProductCommand.Update command = new ProductCommand.Update(2L, "슬리퍼", 50000, 30);
        ZonedDateTime now = ZonedDateTime.now();
        ProductInfo expectedInfo = new ProductInfo(1L, 2L, "슬리퍼", 50000, 30, now, now);
        given(productService.updateProduct(eq(1L), eq(command))).willReturn(expectedInfo);

        // when
        ProductInfo result = productFacade.updateProduct(1L, command);

        // then
        assertThat(result.name()).isEqualTo("슬리퍼");
        assertThat(result.price()).isEqualTo(50000);
    }

    @Test
    void 존재하지_않는_브랜드로_상품을_수정하면_CoreException_NOT_FOUND가_발생한다() {
        // given
        ProductCommand.Update command = new ProductCommand.Update(999L, "슬리퍼", 50000, 30);
        willThrow(new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."))
                .given(brandService).validateBrandExists(999L);

        // when & then
        assertThatThrownBy(() -> productFacade.updateProduct(1L, command))
                .isInstanceOf(CoreException.class);
    }
}
