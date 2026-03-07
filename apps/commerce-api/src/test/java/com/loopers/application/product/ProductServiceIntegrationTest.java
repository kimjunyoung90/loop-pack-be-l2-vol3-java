package com.loopers.application.product;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.command.BrandCreateCommand;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.application.product.command.ProductCreateCommand;
import com.loopers.application.product.command.ProductUpdateCommand;
import com.loopers.application.product.result.ProductResult;
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
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Test
    void 상품_등록_조회_수정_삭제_전체_흐름을_검증한다() {
        // 브랜드 등록
        BrandResult brandResult = brandService.createBrand(new BrandCreateCommand("나이키"));

        // 상품 등록
        ProductCreateCommand createCommand = new ProductCreateCommand(brandResult.id(), "운동화", 100000, 50);
        ProductResult created = productService.createProduct(brandResult.id(), createCommand);
        assertThat(created.name()).isEqualTo("운동화");
        assertThat(created.price()).isEqualTo(100000);
        assertThat(created.stock()).isEqualTo(50);
        assertThat(created.id()).isNotNull();

        // 상품 조회
        ProductResult found = productService.getProduct(created.id());
        assertThat(found.name()).isEqualTo("운동화");

        // 상품 수정
        BrandResult brandResult2 = brandService.createBrand(new BrandCreateCommand("아디다스"));
        ProductUpdateCommand updateCommand = new ProductUpdateCommand(brandResult2.id(), "슬리퍼", 50000, 30);
        ProductResult updated = productService.updateProduct(created.id(), brandResult2.id(), updateCommand);
        assertThat(updated.brandId()).isEqualTo(brandResult2.id());
        assertThat(updated.name()).isEqualTo("슬리퍼");
        assertThat(updated.price()).isEqualTo(50000);
        assertThat(updated.stock()).isEqualTo(30);

        // 상품 삭제
        productService.deleteProduct(created.id());

        // 삭제 후 조회 시 NOT_FOUND
        assertThatThrownBy(() -> productService.getProduct(created.id()))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 삭제된_상품은_목록에서_제외된다() {
        // given
        BrandResult brandResult = brandService.createBrand(new BrandCreateCommand("나이키"));
        ProductResult product1 = productService.createProduct(
                brandResult.id(), new ProductCreateCommand(brandResult.id(), "운동화", 100000, 50));
        ProductResult product2 = productService.createProduct(
                brandResult.id(), new ProductCreateCommand(brandResult.id(), "슬리퍼", 50000, 30));
        productService.deleteProduct(product1.id());

        // when
        Page<ProductResult> products = productService.getProducts(PageRequest.of(0, 20));

        // then
        assertThat(products.getContent()).hasSize(1);
        assertThat(products.getContent().getFirst().name()).isEqualTo("슬리퍼");
    }
}
