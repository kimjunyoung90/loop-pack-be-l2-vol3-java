package com.loopers.domain.coupon;

public interface CouponQuantityRepository {

    long increment(Long couponId);

    void set(Long couponId, int quantity);
}
