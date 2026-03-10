package com.loopers.application.product;

import com.loopers.application.brand.BrandService;
import com.loopers.application.product.command.ProductCreateCommand;
import com.loopers.application.product.command.ProductUpdateCommand;
import com.loopers.application.product.result.ProductResult;
import com.loopers.support.error.CoreException;
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

@ExtendWith(MockitoExtension.class)
class ProductFacadeTest {

    @Mock
    private ProductService productService;

    @Mock
    private BrandService brandService;

    @InjectMocks
    private ProductFacade productFacade;

    @Test
    void 존재하는_브랜드로_상품을_생성하면_브랜드_검증_후_ProductResult를_반환한다() {
        // given
        ProductCreateCommand command = new ProductCreateCommand(1L, "운동화", 100000, 50);
        ZonedDateTime now = ZonedDateTime.now();
        ProductResult expectedResult = new ProductResult(1L, 1L, "운동화", 100000, 50, 0, now, now);
        given(brandService.existsBrand(1L)).willReturn(true);
        given(productService.registerProduct(eq(1L), eq(command))).willReturn(expectedResult);

        // when
        ProductResult result = productFacade.registerProduct(command);

        // then
        assertThat(result.name()).isEqualTo("운동화");
        assertThat(result.price()).isEqualTo(100000);
    }

    @Test
    void 존재하지_않는_브랜드로_상품을_생성하면_예외가_발생한다() {
        // given
        ProductCreateCommand command = new ProductCreateCommand(999L, "운동화", 100000, 50);
        given(brandService.existsBrand(999L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> productFacade.registerProduct(command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 존재하는_브랜드로_상품을_수정하면_브랜드_검증_후_ProductResult를_반환한다() {
        // given
        ProductUpdateCommand command = new ProductUpdateCommand(2L, "슬리퍼", 50000, 30);
        ZonedDateTime now = ZonedDateTime.now();
        ProductResult expectedResult = new ProductResult(1L, 2L, "슬리퍼", 50000, 30, 0, now, now);
        given(brandService.existsBrand(2L)).willReturn(true);
        given(productService.modifyProduct(eq(1L), eq(2L), eq(command))).willReturn(expectedResult);

        // when
        ProductResult result = productFacade.modifyProduct(1L, command);

        // then
        assertThat(result.name()).isEqualTo("슬리퍼");
        assertThat(result.price()).isEqualTo(50000);
    }

    @Test
    void 존재하지_않는_브랜드로_상품을_수정하면_예외가_발생한다() {
        // given
        ProductUpdateCommand command = new ProductUpdateCommand(999L, "슬리퍼", 50000, 30);
        given(brandService.existsBrand(999L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> productFacade.modifyProduct(1L, command))
                .isInstanceOf(CoreException.class);
    }
}
