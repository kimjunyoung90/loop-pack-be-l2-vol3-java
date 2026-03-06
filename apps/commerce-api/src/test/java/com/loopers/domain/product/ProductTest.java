package com.loopers.domain.product;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @Test
    void 상품_정보를_변경하면_brandId_name_price_stock이_모두_변경된다() {
        // given
        Product product = Product.builder()
                .brandId(1L)
                .name("운동화")
                .price(100000)
                .stock(50)
                .build();

        // when
        product.update(2L, "슬리퍼", 50000, 30);

        // then
        assertThat(product.getBrandId()).isEqualTo(2L);
        assertThat(product.getName()).isEqualTo("슬리퍼");
        assertThat(product.getPrice()).isEqualTo(50000);
        assertThat(product.getStock()).isEqualTo(30);
    }

    @Test
    void 상품을_삭제하면_deletedAt이_설정된다() {
        // given
        Product product = Product.builder()
                .brandId(1L)
                .name("운동화")
                .price(100000)
                .stock(50)
                .build();

        // when
        product.delete();

        // then
        assertThat(product.getDeletedAt()).isNotNull();
    }
}