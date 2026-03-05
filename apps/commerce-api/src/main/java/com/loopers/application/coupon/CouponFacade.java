package com.loopers.application.coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CouponFacade {

    private final CouponService couponService;
    private final UserCouponService userCouponService;

    @Transactional
    public UserCouponInfo issueCoupon(IssueCouponCommand command) {
        CouponInfo couponInfo = couponService.getIssuableCoupon(command.couponId());
        return userCouponService.createUserCoupon(command.userId(), couponInfo);
    }

    @Transactional(readOnly = true)
    public List<UserCouponInfo> getMyCoupons(Long userId) {
        return userCouponService.getUserCoupons(userId);
    }

    @Transactional(readOnly = true)
    public Page<UserCouponInfo> getIssuedCoupons(Long couponId, Pageable pageable) {
        return userCouponService.getUserCouponsByCouponId(couponId, pageable);
    }
}
