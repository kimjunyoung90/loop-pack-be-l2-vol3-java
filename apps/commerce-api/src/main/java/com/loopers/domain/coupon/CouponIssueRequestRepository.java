package com.loopers.domain.coupon;

import java.util.Optional;

public interface CouponIssueRequestRepository {

    CouponIssueRequest save(CouponIssueRequest request);

    Optional<CouponIssueRequest> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByUserIdAndCouponIdAndStatusAndDeletedAtIsNull(Long userId, Long couponId, CouponRequestStatus status);
}
