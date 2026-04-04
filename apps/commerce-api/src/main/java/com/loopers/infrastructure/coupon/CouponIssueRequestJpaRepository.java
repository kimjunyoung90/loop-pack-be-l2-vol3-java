package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponIssueRequest;
import com.loopers.domain.coupon.CouponRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponIssueRequestJpaRepository extends JpaRepository<CouponIssueRequest, Long> {

    Optional<CouponIssueRequest> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByUserIdAndCouponIdAndStatusAndDeletedAtIsNull(Long userId, Long couponId, CouponRequestStatus status);
}
