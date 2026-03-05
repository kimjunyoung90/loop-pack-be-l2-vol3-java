package com.loopers.application.coupon;

import com.loopers.domain.coupon.DiscountType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CouponFacadeTest {

    @Mock
    private CouponService couponService;

    @Mock
    private UserCouponService userCouponService;

    @InjectMocks
    private CouponFacade couponFacade;

    @Test
    void 쿠폰_발급_시_정상적으로_사용자_쿠폰이_생성된다() {
        // given
        LocalDate expiredAt = LocalDate.now().plusDays(7);
        CouponInfo couponInfo = new CouponInfo(1L, "테스트 쿠폰", DiscountType.FIXED, 1000, null, expiredAt, null, null);
        UserCouponInfo userCouponInfo = new UserCouponInfo(1L, 1L, 1L, "테스트 쿠폰", DiscountType.FIXED, 1000, null, "AVAILABLE", expiredAt, null, null);

        given(couponService.getIssuableCoupon(1L)).willReturn(couponInfo);
        given(userCouponService.createUserCoupon(eq(1L), any(CouponInfo.class))).willReturn(userCouponInfo);

        IssueCouponCommand command = new IssueCouponCommand(1L, 1L);

        // when
        UserCouponInfo result = couponFacade.issueCoupon(command);

        // then
        assertThat(result.couponName()).isEqualTo("테스트 쿠폰");
        assertThat(result.status()).isEqualTo("AVAILABLE");
    }

    @Test
    void 이미_발급받은_쿠폰을_다시_발급하면_CONFLICT_예외가_발생한다() {
        // given
        LocalDate expiredAt = LocalDate.now().plusDays(7);
        CouponInfo couponInfo = new CouponInfo(1L, "테스트 쿠폰", DiscountType.FIXED, 1000, null, expiredAt, null, null);

        given(couponService.getIssuableCoupon(1L)).willReturn(couponInfo);
        given(userCouponService.createUserCoupon(eq(1L), any(CouponInfo.class)))
                .willThrow(new CoreException(ErrorType.CONFLICT, "이미 발급받은 쿠폰입니다."));

        IssueCouponCommand command = new IssueCouponCommand(1L, 1L);

        // when & then
        assertThatThrownBy(() -> couponFacade.issueCoupon(command))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.CONFLICT));
    }

    @Test
    void 존재하지_않는_쿠폰_발급_시_NOT_FOUND_예외가_발생한다() {
        // given
        given(couponService.getIssuableCoupon(999L)).willThrow(new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        IssueCouponCommand command = new IssueCouponCommand(1L, 999L);

        // when & then
        assertThatThrownBy(() -> couponFacade.issueCoupon(command))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }

    @Test
    void 내_쿠폰_목록_조회_시_UserCouponInfo_리스트를_반환한다() {
        // given
        LocalDate expiredAt = LocalDate.now().plusDays(7);
        UserCouponInfo info = new UserCouponInfo(1L, 1L, 1L, "테스트 쿠폰", DiscountType.FIXED, 1000, null, "AVAILABLE", expiredAt, null, null);
        given(userCouponService.getUserCoupons(1L)).willReturn(List.of(info));

        // when
        List<UserCouponInfo> result = couponFacade.getMyCoupons(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).couponName()).isEqualTo("테스트 쿠폰");
    }

    @Test
    void 내_쿠폰이_없으면_빈_리스트를_반환한다() {
        // given
        given(userCouponService.getUserCoupons(1L)).willReturn(List.of());

        // when
        List<UserCouponInfo> result = couponFacade.getMyCoupons(1L);

        // then
        assertThat(result).isEmpty();
    }
}
