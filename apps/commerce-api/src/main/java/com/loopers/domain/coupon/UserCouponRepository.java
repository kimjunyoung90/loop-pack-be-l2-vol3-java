package com.loopers.domain.coupon;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserCouponRepository {

    UserCoupon save(UserCoupon userCoupon);

    List<UserCoupon> findAllByUserIdAndDeletedAtIsNull(Long userId);

    Page<UserCoupon> findAllByCouponIdAndDeletedAtIsNull(Long couponId, Pageable pageable);

    boolean existsByUserIdAndCouponIdAndDeletedAtIsNull(Long userId, Long couponId);
}
