package com.loopers.domain.coupon;

public interface CouponQuantityRepository {

    boolean addIfAbsent(Long couponId, Long userId);

    long count(Long couponId);

    void remove(Long couponId, Long userId);
}
