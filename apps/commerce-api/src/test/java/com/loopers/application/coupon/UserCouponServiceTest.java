package com.loopers.application.coupon;

import com.loopers.domain.coupon.DiscountType;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserCouponServiceTest {

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private UserCouponService userCouponService;

    @Test
    void 사용자_쿠폰_생성_시_UserCouponInfo를_반환한다() {
        // given
        LocalDate expiredAt = LocalDate.now().plusDays(7);
        CouponInfo couponInfo = new CouponInfo(1L, "테스트 쿠폰", DiscountType.FIXED, 1000, null, expiredAt, null, null);
        given(userCouponRepository.existsByUserIdAndCouponIdAndDeletedAtIsNull(1L, 1L)).willReturn(false);
        given(userCouponRepository.save(any(UserCoupon.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        UserCouponInfo result = userCouponService.createUserCoupon(1L, couponInfo);

        // then
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.couponId()).isEqualTo(1L);
        assertThat(result.couponName()).isEqualTo("테스트 쿠폰");
        assertThat(result.discountType()).isEqualTo(DiscountType.FIXED);
        assertThat(result.discountValue()).isEqualTo(1000);
        assertThat(result.status()).isEqualTo("AVAILABLE");
    }

    @Test
    void 이미_발급받은_쿠폰을_다시_발급하면_CONFLICT_예외가_발생한다() {
        // given
        LocalDate expiredAt = LocalDate.now().plusDays(7);
        CouponInfo couponInfo = new CouponInfo(1L, "테스트 쿠폰", DiscountType.FIXED, 1000, null, expiredAt, null, null);
        given(userCouponRepository.existsByUserIdAndCouponIdAndDeletedAtIsNull(1L, 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userCouponService.createUserCoupon(1L, couponInfo))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.CONFLICT));
    }

    @Test
    void 사용자의_쿠폰_목록을_조회하면_UserCouponInfo_리스트를_반환한다() {
        // given
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(1L)
                .couponId(1L)
                .couponName("테스트 쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();
        given(userCouponRepository.findAllByUserIdAndDeletedAtIsNull(1L)).willReturn(List.of(userCoupon));

        // when
        List<UserCouponInfo> result = userCouponService.getUserCoupons(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).couponName()).isEqualTo("테스트 쿠폰");
        assertThat(result.get(0).status()).isEqualTo("AVAILABLE");
    }

}
