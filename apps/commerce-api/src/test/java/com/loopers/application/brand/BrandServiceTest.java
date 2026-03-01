package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    @Test
    void 유효한_이름으로_브랜드를_생성하면_성공한다() {
        // given
        CreateBrandCommand command = new CreateBrandCommand("나이키");
        Brand brand = Brand.builder().name("나이키").build();
        given(brandRepository.save(any(Brand.class))).willReturn(brand);

        // when
        BrandInfo result = brandService.createBrand(command);

        // then
        assertThat(result.name()).isEqualTo("나이키");
    }

    @Test
    void 존재하는_브랜드를_조회하면_성공한다() {
        // given
        Brand brand = Brand.builder().name("나이키").build();
        given(brandRepository.findById(1L)).willReturn(Optional.of(brand));

        // when
        BrandInfo result = brandService.getBrand(1L);

        // then
        assertThat(result.name()).isEqualTo("나이키");
    }

    @Test
    void 존재하지_않는_브랜드를_조회하면_NOT_FOUND_예외가_발생한다() {
        // given
        given(brandRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> brandService.getBrand(1L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 브랜드_목록을_페이징_조회하면_성공한다() {
        // given
        Brand brand = Brand.builder().name("나이키").build();
        PageRequest pageable = PageRequest.of(0, 20);
        given(brandRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(brand)));

        // when
        Page<BrandInfo> result = brandService.getBrands(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("나이키");
    }

    @Test
    void 존재하는_브랜드를_수정하면_성공한다() {
        // given
        Brand brand = Brand.builder().name("나이키").build();
        given(brandRepository.findById(1L)).willReturn(Optional.of(brand));
        UpdateBrandCommand command = new UpdateBrandCommand("아디다스");

        // when
        BrandInfo result = brandService.updateBrand(1L, command);

        // then
        assertThat(result.name()).isEqualTo("아디다스");
    }

    @Test
    void 존재하지_않는_브랜드를_수정하면_NOT_FOUND_예외가_발생한다() {
        // given
        given(brandRepository.findById(1L)).willReturn(Optional.empty());
        UpdateBrandCommand command = new UpdateBrandCommand("아디다스");

        // when & then
        assertThatThrownBy(() -> brandService.updateBrand(1L, command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 존재하는_브랜드를_삭제하면_성공한다() {
        // given
        Brand brand = Brand.builder().name("나이키").build();
        given(brandRepository.findById(1L)).willReturn(Optional.of(brand));

        // when
        brandService.deleteBrand(1L);

        // then
        assertThat(brand.getDeletedAt()).isNotNull();
    }

    @Test
    void 존재하지_않는_브랜드를_삭제하면_NOT_FOUND_예외가_발생한다() {
        // given
        given(brandRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> brandService.deleteBrand(1L))
                .isInstanceOf(CoreException.class);
    }
}
