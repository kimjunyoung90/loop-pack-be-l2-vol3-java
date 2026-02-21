package com.loopers.domain.brand;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrandTest {

    @Test
    void 브랜드_이름을_변경할_수_있다() {
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
    void 브랜드를_소프트_삭제하면_deletedAt이_설정된다() {
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
