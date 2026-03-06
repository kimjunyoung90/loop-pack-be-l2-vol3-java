package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private final UserCouponJpaRepository userCouponJpaRepository;

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        return userCouponJpaRepository.save(userCoupon);
    }

    @Override
    public List<UserCoupon> findAllByUserIdAndDeletedAtIsNull(Long userId) {
        return userCouponJpaRepository.findAllByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public Page<UserCoupon> findAllByCouponIdAndDeletedAtIsNull(Long couponId, Pageable pageable) {
        return userCouponJpaRepository.findAllByCouponIdAndDeletedAtIsNull(couponId, pageable);
    }

    @Override
    public boolean existsByUserIdAndCouponIdAndDeletedAtIsNull(Long userId, Long couponId) {
        return userCouponJpaRepository.existsByUserIdAndCouponIdAndDeletedAtIsNull(userId, couponId);
    }
}
