package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    void 쿠폰_조회_시_쿠폰이_존재하지_않으면_NOT_FOUND_예외가_발생한다() {
        // given
        given(couponRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.getCoupon(1L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 존재하지_않는_쿠폰_수정_시_NOT_FOUND_예외가_발생한다() {
        // given
        given(couponRepository.findById(1L)).willReturn(Optional.empty());
        UpdateCouponCommand command = new UpdateCouponCommand("쿠폰", DiscountType.FIXED, 1000, null, LocalDate.now().plusDays(7));

        // when & then
        assertThatThrownBy(() -> couponService.updateCoupon(1L, command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 존재하지_않는_쿠폰_삭제_시_NOT_FOUND_예외가_발생한다() {
        // given
        given(couponRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.deleteCoupon(1L))
                .isInstanceOf(CoreException.class);
    }
}
