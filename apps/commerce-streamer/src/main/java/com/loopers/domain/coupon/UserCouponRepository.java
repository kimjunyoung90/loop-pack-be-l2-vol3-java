package com.loopers.domain.coupon;

public interface UserCouponRepository {

    UserCoupon save(UserCoupon userCoupon);

    boolean existsByUserIdAndCouponIdAndDeletedAtIsNull(Long userId, Long couponId);
}
