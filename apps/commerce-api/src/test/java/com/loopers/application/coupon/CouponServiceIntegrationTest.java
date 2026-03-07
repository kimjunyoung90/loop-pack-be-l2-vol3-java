package com.loopers.application.coupon;

import com.loopers.application.coupon.command.CouponCreateCommand;
import com.loopers.application.coupon.command.CouponUpdateCommand;
import com.loopers.application.coupon.result.CouponResult;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.support.error.CoreException;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
@Transactional
class CouponServiceIntegrationTest {

    @Autowired
    private CouponService couponService;

    @Test
    void 쿠폰_생성_조회_수정_삭제_전체_흐름을_검증한다() {
        // 생성
        LocalDate expiredAt = LocalDate.now().plusDays(30);
        CouponCreateCommand createCommand = new CouponCreateCommand(
                "신규 쿠폰", DiscountType.FIXED, 3000, 10000, expiredAt
        );
        CouponResult created = couponService.registerCoupon(createCommand);
        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("신규 쿠폰");
        assertThat(created.discountType()).isEqualTo(DiscountType.FIXED);
        assertThat(created.discountValue()).isEqualTo(3000);
        assertThat(created.minOrderAmount()).isEqualTo(10000);
        assertThat(created.expiredAt()).isEqualTo(expiredAt);

        // 조회
        CouponResult found = couponService.getCoupon(created.id());
        assertThat(found.name()).isEqualTo("신규 쿠폰");

        // 수정
        LocalDate newExpiredAt = LocalDate.now().plusDays(60);
        CouponUpdateCommand updateCommand = new CouponUpdateCommand(
                "수정 쿠폰", DiscountType.RATE, 10, 5000, newExpiredAt
        );
        CouponResult updated = couponService.modifyCoupon(created.id(), updateCommand);
        assertThat(updated.name()).isEqualTo("수정 쿠폰");
        assertThat(updated.discountType()).isEqualTo(DiscountType.RATE);
        assertThat(updated.discountValue()).isEqualTo(10);
        assertThat(updated.minOrderAmount()).isEqualTo(5000);
        assertThat(updated.expiredAt()).isEqualTo(newExpiredAt);

        // 삭제
        couponService.deleteCoupon(created.id());

        // 삭제 후 조회 시 NOT_FOUND
        assertThatThrownBy(() -> couponService.getCoupon(created.id()))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 삭제된_쿠폰은_목록에서_제외된다() {
        // given
        CouponResult coupon1 = couponService.registerCoupon(
                new CouponCreateCommand("쿠폰1", DiscountType.FIXED, 1000, null, LocalDate.now().plusDays(7))
        );
        couponService.registerCoupon(
                new CouponCreateCommand("쿠폰2", DiscountType.RATE, 10, 5000, LocalDate.now().plusDays(14))
        );
        couponService.deleteCoupon(coupon1.id());

        // when
        Page<CouponResult> coupons = couponService.getCoupons(PageRequest.of(0, 20));

        // then
        assertThat(coupons.getContent()).hasSize(1);
        assertThat(coupons.getContent().getFirst().name()).isEqualTo("쿠폰2");
    }

    @Test
    void 삭제된_쿠폰_상세_조회_시_예외가_발생한다() {
        // given
        CouponResult created = couponService.registerCoupon(
                new CouponCreateCommand("쿠폰", DiscountType.FIXED, 1000, null, LocalDate.now().plusDays(7))
        );
        couponService.deleteCoupon(created.id());

        // when & then
        assertThatThrownBy(() -> couponService.getCoupon(created.id()))
                .isInstanceOf(CoreException.class);
    }
}
