package com.loopers.application.coupon;

import com.loopers.domain.coupon.DiscountType;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

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
    void 사용자_쿠폰_생성_시_쿠폰_정보가_DB에_저장된다() {
        // given
        CouponInfo couponInfo = couponService.createCoupon(
                new CreateCouponCommand("테스트 쿠폰", DiscountType.FIXED, 1000, 10000, LocalDate.now().plusDays(7))
        );

        // when
        userCouponService.createUserCoupon(1L, couponInfo);

        // then - DB에서 조회하여 실제 저장 확인
        List<UserCouponInfo> savedCoupons = userCouponService.getUserCoupons(1L);
        assertThat(savedCoupons).hasSize(1);

        UserCouponInfo saved = savedCoupons.getFirst();
        assertThat(saved.userId()).isEqualTo(1L);
        assertThat(saved.couponId()).isEqualTo(couponInfo.id());
        assertThat(saved.couponName()).isEqualTo("테스트 쿠폰");
        assertThat(saved.discountType()).isEqualTo(DiscountType.FIXED);
        assertThat(saved.discountValue()).isEqualTo(1000);
        assertThat(saved.status()).isEqualTo("AVAILABLE");
    }

    @Test
    void 쿠폰_목록_조회_시_해당_사용자의_쿠폰만_조회된다() {
        // given
        CouponInfo coupon1 = couponService.createCoupon(
                new CreateCouponCommand("쿠폰1", DiscountType.FIXED, 1000, 10000, LocalDate.now().plusDays(7))
        );
        CouponInfo coupon2 = couponService.createCoupon(
                new CreateCouponCommand("쿠폰2", DiscountType.FIXED, 2000, 10000, LocalDate.now().plusDays(7))
        );
        userCouponService.createUserCoupon(1L, coupon1);
        userCouponService.createUserCoupon(2L, coupon2);

        // when
        List<UserCouponInfo> result = userCouponService.getUserCoupons(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().couponName()).isEqualTo("쿠폰1");
    }

    @Test
    void 쿠폰별_발급_내역_조회_시_해당_쿠폰의_발급_내역만_페이징_조회된다() {
        // given
        CouponInfo coupon1 = couponService.createCoupon(
                new CreateCouponCommand("쿠폰1", DiscountType.FIXED, 1000, 10000, LocalDate.now().plusDays(7))
        );
        CouponInfo coupon2 = couponService.createCoupon(
                new CreateCouponCommand("쿠폰2", DiscountType.FIXED, 2000, 10000, LocalDate.now().plusDays(7))
        );
        userCouponService.createUserCoupon(1L, coupon1);
        userCouponService.createUserCoupon(2L, coupon1);
        userCouponService.createUserCoupon(3L, coupon2);

        // when
        Page<UserCouponInfo> result = userCouponService.getUserCouponsByCouponId(coupon1.id(), PageRequest.of(0, 20));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(info -> info.couponId().equals(coupon1.id()));
    }
}
