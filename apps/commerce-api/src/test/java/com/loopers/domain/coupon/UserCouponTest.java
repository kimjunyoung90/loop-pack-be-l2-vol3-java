package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserCouponTest {

    private UserCoupon createUserCoupon(LocalDate expiredAt) {
        return createUserCoupon(1L, expiredAt);
    }

    private UserCoupon createUserCoupon(Long userId, LocalDate expiredAt) {
        return UserCoupon.builder()
                .userId(userId)
                .couponId(1L)
                .couponName("테스트 쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .minOrderAmount(10000)
                .expiredAt(expiredAt)
                .build();
    }

    @Test
    void 사용자_쿠폰_생성_시_상태는_AVAILABLE이다() {
        // when
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().plusDays(7));

        // then
        assertThat(userCoupon.getStatus()).isEqualTo(CouponStatus.AVAILABLE);
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
    void 이미_사용된_쿠폰을_다시_사용하면_CoreException_BAD_REQUEST가_발생한다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().plusDays(7));
        userCoupon.use();

        // when & then
        assertThatThrownBy(userCoupon::use)
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 만료된_쿠폰을_사용하면_상태가_EXPIRED로_변경되고_BAD_REQUEST가_발생한다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().minusDays(1));

        // when & then
        assertThatThrownBy(userCoupon::use)
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
        assertThat(userCoupon.getStatus()).isEqualTo(CouponStatus.EXPIRED);
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

    // --- 쿠폰 사용 가능 여부 검증 테스트 ---

    @Test
    void 본인_소유가_아닌_쿠폰의_사용_가능_여부_검증_시_FORBIDDEN이_발생한다() {
        // given
        Long couponOwnerUserId = 1L;
        Long otherUserId = 999L;
        UserCoupon userCoupon = createUserCoupon(couponOwnerUserId, LocalDate.now().plusDays(7));

        // when & then
        assertThatThrownBy(() -> userCoupon.validateUsable(otherUserId, 50000))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.FORBIDDEN));
    }

    @Test
    void 이미_사용된_쿠폰의_사용_가능_여부_검증_시_BAD_REQUEST가_발생한다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().plusDays(7));
        userCoupon.use();

        // when & then
        assertThatThrownBy(() -> userCoupon.validateUsable(1L, 50000))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 만료된_쿠폰의_사용_가능_여부_검증_시_상태가_EXPIRED로_변경되고_BAD_REQUEST가_발생한다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().minusDays(1));

        // when & then
        assertThatThrownBy(() -> userCoupon.validateUsable(1L, 50000))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
        assertThat(userCoupon.getStatus()).isEqualTo(CouponStatus.EXPIRED);
    }

    @Test
    void 주문_금액이_최소_주문_금액_미만이면_사용_가능_여부_검증_시_BAD_REQUEST가_발생한다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().plusDays(7)); // minOrderAmount = 10000

        // when & then
        assertThatThrownBy(() -> userCoupon.validateUsable(1L, 5000))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 유효한_쿠폰의_사용_가능_여부_검증_시_예외가_발생하지_않는다() {
        // given
        UserCoupon userCoupon = createUserCoupon(LocalDate.now().plusDays(7)); // minOrderAmount = 10000

        // when & then
        assertThatCode(() -> userCoupon.validateUsable(1L, 50000))
                .doesNotThrowAnyException();
    }

    // --- 할인 금액 계산 테스트 ---

    @Test
    void 정률_할인_쿠폰의_할인_금액은_주문_금액에_비율을_적용한_값이다() {
        // given
        int discountRate = 10;
        int totalAmount = 50000;
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(1L)
                .couponId(1L)
                .couponName("정률 할인 쿠폰")
                .discountType(DiscountType.RATE)
                .discountValue(discountRate)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();

        // when
        int discount = userCoupon.calculateDiscount(totalAmount);

        // then
        assertThat(discount).isEqualTo(totalAmount * discountRate / 100);
    }

    @Test
    void 정률_할인_금액_계산_시_소수점_이하는_절사된다() {
        // given
        int discountRate = 10;
        int totalAmount = 33333; // 33333 * 10 / 100 = 3333.3 → 3333
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(1L)
                .couponId(1L)
                .couponName("정률 할인 쿠폰")
                .discountType(DiscountType.RATE)
                .discountValue(discountRate)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();

        // when
        int discount = userCoupon.calculateDiscount(totalAmount);

        // then
        int expectedWithoutTruncation = totalAmount * discountRate; // 333330
        assertThat(expectedWithoutTruncation % 100).isNotZero(); // 소수점이 발생하는 케이스임을 보장
        assertThat(discount).isEqualTo(totalAmount * discountRate / 100);
    }

    @Test
    void 할인_금액이_주문_총액을_초과하면_주문_총액을_할인_금액으로_반환한다() {
        // given
        int discountValue = 100000;
        int totalAmount = 5000;
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(1L)
                .couponId(1L)
                .couponName("대형 할인 쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(discountValue)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();

        // when
        int discount = userCoupon.calculateDiscount(totalAmount);

        // then
        assertThat(discountValue).isGreaterThan(totalAmount); // 할인값이 총액을 초과하는 케이스임을 보장
        assertThat(discount).isEqualTo(totalAmount);
    }
}
