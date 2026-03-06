package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCouponJpaRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findAllByUserIdAndDeletedAtIsNull(Long userId);

    Page<UserCoupon> findAllByCouponIdAndDeletedAtIsNull(Long couponId, Pageable pageable);

    boolean existsByUserIdAndCouponIdAndDeletedAtIsNull(Long userId, Long couponId);
}
