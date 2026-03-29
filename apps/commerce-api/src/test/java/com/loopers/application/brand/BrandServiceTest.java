package com.loopers.application.brand;

import com.loopers.application.brand.command.BrandUpdateCommand;
import com.loopers.domain.brand.BrandCacheRepository;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.cache.CacheLockManager;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandCacheRepository brandCacheRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CacheLockManager cacheLockManager;

    @InjectMocks
    private BrandService brandService;

    @Test
    void 존재하지_않는_브랜드_조회_시_예외가_발생한다() {
        // given
        given(brandCacheRepository.getBrand(1L)).willReturn(Optional.empty());
        given(cacheLockManager.executeWithLock(anyString(), any(), any()))
                .willAnswer(invocation -> {
                    java.util.function.Supplier<?> dbFallback = invocation.getArgument(2);
                    return dbFallback.get();
                });
        given(brandRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> brandService.getBrand(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }

    @Test
    void 존재하지_않는_브랜드_수정_시_예외가_발생한다() {
        // given
        given(brandRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.empty());
        BrandUpdateCommand command = new BrandUpdateCommand("아디다스");

        // when & then
        assertThatThrownBy(() -> brandService.modifyBrand(1L, command))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }

    @Test
    void 존재하지_않는_브랜드_삭제_시_예외가_발생한다() {
        // given
        given(brandRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> brandService.deleteBrand(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }
}
