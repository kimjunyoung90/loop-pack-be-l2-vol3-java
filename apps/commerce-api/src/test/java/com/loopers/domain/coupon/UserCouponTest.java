package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserCouponTest {

    private UserCoupon createUserCoupon(LocalDate expiredAt) {
        return UserCoupon.builder()
                .userId(1L)
                .couponId(1L)
                .couponName("테스트 쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .minOrderAmount(10000)
                .expiredAt(expiredAt)
                .build();
    }

    @Test
    void 사용자_쿠폰_생성_시_쿠폰_상태는_AVAILABLE이다() {
        // given
        Coupon coupon = Coupon.builder()
                .name("신규 가입 쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(3000)
                .minOrderAmount(10000)
                .expiredAt(LocalDate.now().plusDays(30))
                .build();

        // when
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(1L)
                .couponId(coupon.getId())
                .couponName(coupon.getName())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .expiredAt(coupon.getExpiredAt())
                .build();

        // then
        assertThat(userCoupon.getStatus()).isEqualTo(CouponStatus.AVAILABLE);
        assertThat(userCoupon.getCouponName()).isEqualTo(coupon.getName());
        assertThat(userCoupon.getDiscountType()).isEqualTo(coupon.getDiscountType());
        assertThat(userCoupon.getDiscountValue()).isEqualTo(coupon.getDiscountValue());
        assertThat(userCoupon.getMinOrderAmount()).isEqualTo(coupon.getMinOrderAmount());
        assertThat(userCoupon.getExpiredAt()).isEqualTo(coupon.getExpiredAt());
    }

    @Test
    void AVAILABLE_상태의_쿠폰을_사용하면_USED_상태가_된다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().plusDays(7));

        // when
        userCoupon.use();

        // then
        assertThat(userCoupon.getStatus()).isEqualTo(CouponStatus.USED);
    }

    @Test
    void 이미_사용된_쿠폰을_다시_사용하면_예외가_발생한다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().plusDays(7));
        userCoupon.use();

        // when & then
        assertThatThrownBy(userCoupon::use)
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 만료된_쿠폰을_사용하면_예외가_발생한다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().minusDays(1));

        // when & then
        assertThatThrownBy(userCoupon::use)
                .isInstanceOf(CoreException.class);
    }

    @Test
    void USED_상태의_쿠폰을_복원하면_AVAILABLE_상태가_된다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().plusDays(7));
        userCoupon.use();

        // when
        userCoupon.restore();

        // then
        assertThat(userCoupon.getStatus()).isEqualTo(CouponStatus.AVAILABLE);
    }

    @Test
    void 만료일이_오늘_이전이면_만료된_쿠폰이다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().minusDays(1));

        // when & then
        assertThat(userCoupon.isExpired()).isTrue();
    }

    @Test
    void 만료일이_오늘이면_만료되지_않은_쿠폰이다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now());

        // when & then
        assertThat(userCoupon.isExpired()).isFalse();
    }

    @Test
    void AVAILABLE_상태이고_만료되지_않으면_사용_가능하다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().plusDays(7));

        // when & then
        assertThat(userCoupon.isAvailable()).isTrue();
    }

    @Test
    void USED_상태이면_사용_불가능하다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().plusDays(7));
        userCoupon.use();

        // when & then
        assertThat(userCoupon.isAvailable()).isFalse();
    }

    @Test
    void 만료된_쿠폰은_사용_불가능하다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().minusDays(1));

        // when & then
        assertThat(userCoupon.isAvailable()).isFalse();
    }
}
