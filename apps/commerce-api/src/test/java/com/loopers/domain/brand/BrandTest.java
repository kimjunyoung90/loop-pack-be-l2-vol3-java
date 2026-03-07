package com.loopers.domain.brand;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrandTest {

    @Test
    void 브랜드_이름_변경_시_name이_변경된다() {
        // given
        Brand brand = Brand.builder()
                .name("나이키")
                .build();

        // when
        brand.update("아디다스");

        // then
        assertThat(brand.getName()).isEqualTo("아디다스");
    }

    @Test
    void 브랜드를_삭제하면_삭제_처리된다() {
        // given
        Brand brand = Brand.builder()
                .name("나이키")
                .build();

        // when
        brand.delete();

        // then
        assertThat(brand.getDeletedAt()).isNotNull();
    }
}
