package com.loopers.application.brand;

import com.loopers.domain.brand.BrandRepository;
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
class BrandServiceIntegrationTest {

    @Autowired
    private BrandService brandService;

    @Autowired
    private BrandRepository brandRepository;

    @Test
    void 브랜드_등록_조회_수정_삭제_전체_흐름을_검증한다() {
        // 등록
        BrandCommand.Create createCommand = new BrandCommand.Create("나이키");
        BrandInfo created = brandService.createBrand(createCommand);
        assertThat(created.name()).isEqualTo("나이키");
        assertThat(created.id()).isNotNull();

        // 조회
        BrandInfo found = brandService.getBrand(created.id());
        assertThat(found.name()).isEqualTo("나이키");

        // 수정
        BrandCommand.Update updateCommand = new BrandCommand.Update("아디다스");
        BrandInfo updated = brandService.updateBrand(created.id(), updateCommand);
        assertThat(updated.name()).isEqualTo("아디다스");

        // 삭제
        brandService.deleteBrand(created.id());

        // 삭제 후 조회 시 NOT_FOUND
        assertThatThrownBy(() -> brandService.getBrand(created.id()))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 삭제된_브랜드는_목록에서_제외된다() {
        // given
        BrandInfo brand1 = brandService.createBrand(new BrandCommand.Create("나이키"));
        BrandInfo brand2 = brandService.createBrand(new BrandCommand.Create("아디다스"));
        brandService.deleteBrand(brand1.id());

        // when
        Page<BrandInfo> brands = brandService.getBrands(PageRequest.of(0, 20));

        // then
        assertThat(brands.getContent()).hasSize(1);
        assertThat(brands.getContent().getFirst().name()).isEqualTo("아디다스");
    }
}
