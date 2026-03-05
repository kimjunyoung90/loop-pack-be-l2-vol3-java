package com.loopers.application.coupon;

import com.loopers.domain.coupon.DiscountType;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
@Transactional
class UserCouponServiceIntegrationTest {

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private CouponService couponService;

    @Test
    void 쿠폰_발급_내역이_존재하면_true를_반환한다() {
        // given
        CouponInfo couponInfo = couponService.createCoupon(
                new CreateCouponCommand("테스트 쿠폰", DiscountType.FIXED, 1000, 10000, LocalDate.now().plusDays(7))
        );
        userCouponService.createUserCoupon(1L, couponInfo);

        // when
        boolean result = userCouponService.existsByCouponId(couponInfo.id());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 쿠폰_발급_내역이_없으면_false를_반환한다() {
        // given
        CouponInfo couponInfo = couponService.createCoupon(
                new CreateCouponCommand("테스트 쿠폰", DiscountType.FIXED, 1000, 10000, LocalDate.now().plusDays(7))
        );

        // when
        boolean result = userCouponService.existsByCouponId(couponInfo.id());

        // then
        assertThat(result).isFalse();
    }
}
