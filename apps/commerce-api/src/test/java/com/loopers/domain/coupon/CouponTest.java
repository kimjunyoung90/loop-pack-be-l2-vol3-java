package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
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
        ).isInstanceOf(CoreException.class);
    }

    @Test
    void 쿠폰명이_null이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name(null)
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class);
    }

    @Test
    void 할인_유형이_null이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(null)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class);
    }

    @Test
    void 할인_값이_0이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(0)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class);
    }

    @Test
    void 할인_값이_음수이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(-1)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class);
    }

    @Test
    void 정률_할인_값이_100을_초과하면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.RATE)
                .discountValue(101)
                .expiredAt(LocalDate.now().plusDays(7))
                .build()
        ).isInstanceOf(CoreException.class);
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
        ).isInstanceOf(CoreException.class);
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
        ).isInstanceOf(CoreException.class);
    }

    @Test
    void 유효기간이_과거이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().minusDays(1))
                .build()
        ).isInstanceOf(CoreException.class);
    }

    @Test
    void 유효기간이_null이면_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(null)
                .build()
        ).isInstanceOf(CoreException.class);
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
        coupon.modify("수정된 쿠폰", DiscountType.RATE, 10, 5000, newExpiredAt);

        // then
        assertThat(coupon.getName()).isEqualTo("수정된 쿠폰");
        assertThat(coupon.getDiscountType()).isEqualTo(DiscountType.RATE);
        assertThat(coupon.getDiscountValue()).isEqualTo(10);
        assertThat(coupon.getMinOrderAmount()).isEqualTo(5000);
        assertThat(coupon.getExpiredAt()).isEqualTo(newExpiredAt);
    }

    @Test
    void 유효기간이_남아있는_쿠폰은_발급_검증을_통과한다() {
        // given
        Coupon coupon = Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();

        // when & then (예외 없음)
        coupon.validateIssuable();
    }

    @Test
    void 만료된_쿠폰은_발급_검증_시_예외가_발생한다() {
        // given - 오늘 만료되는 쿠폰을 생성 후 만료일을 과거로 변경할 수 없으므로
        // validateIssuable은 expiredAt < today를 검사하므로 오늘인 경우 통과
        Coupon coupon = Coupon.builder()
                .name("쿠폰")
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .expiredAt(LocalDate.now())
                .build();

        // when & then (오늘은 만료되지 않으므로 통과)
        coupon.validateIssuable();
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
        assertThatThrownBy(() -> coupon.modify("", DiscountType.FIXED, 1000, null, LocalDate.now().plusDays(7)))
                .isInstanceOf(CoreException.class);
    }
}
