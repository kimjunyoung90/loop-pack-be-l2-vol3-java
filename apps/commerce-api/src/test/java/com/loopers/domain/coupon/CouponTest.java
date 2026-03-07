package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    @Test
    void 유효한_정보로_쿠폰_생성_시_쿠폰이_생성된다() {
        // given
        LocalDate expiredAt = LocalDate.now().plusDays(30);

        // when
        Coupon coupon = Coupon.builder()
                .name("신규 가입 쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(3000)
                .minOrderAmount(10000)
                .expiredAt(expiredAt)
                .build();

        // then
        assertThat(coupon.getName()).isEqualTo("신규 가입 쿠폰");
        assertThat(coupon.getDiscountType()).isEqualTo(DiscountType.FIXED);
        assertThat(coupon.getDiscountValue()).isEqualTo(3000);
        assertThat(coupon.getMinOrderAmount()).isEqualTo(10000);
        assertThat(coupon.getExpiredAt()).isEqualTo(expiredAt);
    }

    @Test
    void 최소_주문_금액_없이_쿠폰_생성_시_쿠폰이_생성된다() {
        // when
        Coupon coupon = Coupon.builder()
                .name("무조건 할인 쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .minOrderAmount(null)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();

        // then
        assertThat(coupon.getMinOrderAmount()).isNull();
    }

    @Test
    void 만료일을_오늘로_지정한_경우_쿠폰_생성_시_쿠폰이_생성된다() {
        // when
        Coupon coupon = Coupon.builder()
                .name("당일 프로모션 쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now())
                .build();

        // then
        assertThat(coupon.getExpiredAt()).isEqualTo(LocalDate.now());
    }

    @Test
    void 쿠폰명이_비어있으면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 쿠폰명이_null이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name(null)
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 할인_유형이_null이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(null)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 할인_값이_0이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(0)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 할인_값이_음수이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(-1)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 정률_할인_값이_100을_초과하면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.RATE)
                .discountValue(101)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 정률_할인_값이_100인_쿠폰_생성_시_쿠폰이_생성된다() {
        // when
        Coupon coupon = Coupon.builder()
                .name("100% 할인 쿠폰")
                .discountType(DiscountType.RATE)
                .discountValue(100)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();

        // then
        assertThat(coupon.getDiscountValue()).isEqualTo(100);
    }

    @Test
    void 정액_할인_값이_100을_초과해도_쿠폰_생성_시_쿠폰이_생성된다() {
        // when
        Coupon coupon = Coupon.builder()
                .name("대량 할인 쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(50000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();

        // then
        assertThat(coupon.getDiscountValue()).isEqualTo(50000);
    }

    @Test
    void 최소_주문_금액이_0이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .minOrderAmount(0)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 최소_주문_금액이_음수이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .minOrderAmount(-1)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 유효기간이_과거이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().minusDays(1))
                .build()
        ).isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 유효기간이_null이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(null)
                .build()
        ).isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 유효한_정보로_쿠폰_수정_시_쿠폰이_수정된다() {
        // given
        Coupon coupon = Coupon.builder()
                .name("기존 쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();

        // when
        LocalDate newExpiredAt = LocalDate.now().plusDays(30);
        coupon.changeInfo("수정된 쿠폰", DiscountType.RATE, 10, 5000, newExpiredAt);

        // then
        assertThat(coupon.getName()).isEqualTo("수정된 쿠폰");
        assertThat(coupon.getDiscountType()).isEqualTo(DiscountType.RATE);
        assertThat(coupon.getDiscountValue()).isEqualTo(10);
        assertThat(coupon.getMinOrderAmount()).isEqualTo(5000);
        assertThat(coupon.getExpiredAt()).isEqualTo(newExpiredAt);
    }

    @Test
    void 유효기간이_남아있는_쿠폰은_발급_시_예외가_발생하지_않는다() {
        // given
        Coupon coupon = Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();
        ReflectionTestUtils.setField(coupon, "id", 1L);

        // when & then (예외 없음)
        assertThatCode(() -> coupon.issue(1L)).doesNotThrowAnyException();
    }

    @Test
    void 유효기간이_지난_쿠폰은_발급_시_예외가_발생한다() {
        // given
        Coupon coupon = Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();
        ReflectionTestUtils.setField(coupon, "id", 1L);
        ReflectionTestUtils.setField(coupon, "expiredAt", LocalDate.now().minusDays(1));

        // when & then
        assertThatThrownBy(() -> coupon.issue(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 삭제된_쿠폰은_발급_시_예외가_발생한다() {
        // given
        Coupon coupon = Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();
        ReflectionTestUtils.setField(coupon, "id", 1L);
        coupon.delete();

        // when & then
        assertThatThrownBy(() -> coupon.issue(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }

    @Test
    void 수정_시_쿠폰명이_비어있으면_예외가_발생한다() {
        // given
        Coupon coupon = Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();

        // when & then
        assertThatThrownBy(() -> coupon.changeInfo("", DiscountType.FIXED, 1000, null, LocalDate.now().plusDays(7)))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 저장되지_않은_쿠폰을_발급하면_예외가_발생한다() {
        // given
        Coupon coupon = Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();

        // when & then
        assertThatThrownBy(() -> coupon.issue(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void userId가_null이면_쿠폰_발급_시_예외가_발생한다() {
        // given
        Coupon coupon = Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();
        ReflectionTestUtils.setField(coupon, "id", 1L);

        // when & then
        assertThatThrownBy(() -> coupon.issue(null))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    void 저장된_쿠폰을_발급하면_UserCoupon이_생성된다() {
        // given
        Coupon coupon = Coupon.builder()
                .name("신규 가입 쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(3000)
                .minOrderAmount(10000)
                .expiredAt(LocalDate.now().plusDays(30))
                .build();
        ReflectionTestUtils.setField(coupon, "id", 1L);

        // when
        UserCoupon userCoupon = coupon.issue(5L);

        // then
        assertThat(userCoupon.getUserId()).isEqualTo(5L);
        assertThat(userCoupon.getCouponId()).isEqualTo(1L);
        assertThat(userCoupon.getCouponName()).isEqualTo("신규 가입 쿠폰");
        assertThat(userCoupon.getDiscountType()).isEqualTo(DiscountType.FIXED);
        assertThat(userCoupon.getDiscountValue()).isEqualTo(3000);
        assertThat(userCoupon.getMinOrderAmount()).isEqualTo(10000);
        assertThat(userCoupon.getStatus()).isEqualTo(CouponStatus.AVAILABLE);
    }
}
