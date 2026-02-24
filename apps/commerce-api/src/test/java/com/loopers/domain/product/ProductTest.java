package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @Test
    void 상품_정보를_변경하면_brand_name_price_stock이_모두_변경된다() {
        // given
        Brand brand1 = Brand.builder().name("나이키").build();
        Brand brand2 = Brand.builder().name("아디다스").build();
        Product product = Product.builder()
                .brand(brand1)
                .name("운동화")
                .price(100000)
                .stock(50)
                .build();

        // when
        product.update(brand2, "슬리퍼", 50000, 30);

        // then
        assertThat(product.getBrand()).isEqualTo(brand2);
        assertThat(product.getName()).isEqualTo("슬리퍼");
        assertThat(product.getPrice()).isEqualTo(50000);
        assertThat(product.getStock()).isEqualTo(30);
    }

    @Test
    void 상품을_삭제하면_deletedAt이_설정된다() {
        // given
        Brand brand = Brand.builder().name("나이키").build();
        Product product = Product.builder()
                .brand(brand)
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