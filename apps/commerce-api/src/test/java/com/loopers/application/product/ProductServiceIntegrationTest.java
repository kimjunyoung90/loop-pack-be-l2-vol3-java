package com.loopers.application.product;

import com.loopers.application.brand.BrandInfo;
import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.CreateBrandCommand;
import com.loopers.domain.brand.Brand;
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
        BrandInfo brandInfo = brandService.createBrand(new CreateBrandCommand("나이키"));
        Brand brand = brandService.findBrand(brandInfo.id());

        // 상품 등록
        CreateProductCommand createCommand = new CreateProductCommand(brandInfo.id(), "운동화", 100000, 50);
        ProductInfo created = productService.createProduct(brand, createCommand);
        assertThat(created.name()).isEqualTo("운동화");
        assertThat(created.price()).isEqualTo(100000);
        assertThat(created.stock()).isEqualTo(50);
        assertThat(created.id()).isNotNull();

        // 상품 조회
        ProductInfo found = productService.getProduct(created.id());
        assertThat(found.name()).isEqualTo("운동화");

        // 상품 수정
        BrandInfo brandInfo2 = brandService.createBrand(new CreateBrandCommand("아디다스"));
        Brand brand2 = brandService.findBrand(brandInfo2.id());
        UpdateProductCommand updateCommand = new UpdateProductCommand(brandInfo2.id(), "슬리퍼", 50000, 30);
        ProductInfo updated = productService.updateProduct(created.id(), brand2, updateCommand);
        assertThat(updated.brandId()).isEqualTo(brandInfo2.id());
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
        BrandInfo brandInfo = brandService.createBrand(new CreateBrandCommand("나이키"));
        Brand brand = brandService.findBrand(brandInfo.id());
        ProductInfo product1 = productService.createProduct(
                brand, new CreateProductCommand(brandInfo.id(), "운동화", 100000, 50));
        ProductInfo product2 = productService.createProduct(
                brand, new CreateProductCommand(brandInfo.id(), "슬리퍼", 50000, 30));
        productService.deleteProduct(product1.id());

        // when
        Page<ProductInfo> products = productService.getProducts(PageRequest.of(0, 20));

        // then
        assertThat(products.getContent()).hasSize(1);
        assertThat(products.getContent().getFirst().name()).isEqualTo("슬리퍼");
    }
}
