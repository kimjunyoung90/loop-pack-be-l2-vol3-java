package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponIssueRequest;
import com.loopers.domain.coupon.CouponIssueRequestRepository;
import com.loopers.domain.coupon.CouponRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CouponIssueRequestRepositoryImpl implements CouponIssueRequestRepository {

    private final CouponIssueRequestJpaRepository couponIssueRequestJpaRepository;

    @Override
    public CouponIssueRequest save(CouponIssueRequest request) {
        return couponIssueRequestJpaRepository.save(request);
    }

    @Override
    public Optional<CouponIssueRequest> findByIdAndDeletedAtIsNull(Long id) {
        return couponIssueRequestJpaRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public boolean existsByUserIdAndCouponIdAndStatusAndDeletedAtIsNull(Long userId, Long couponId, CouponRequestStatus status) {
        return couponIssueRequestJpaRepository.existsByUserIdAndCouponIdAndStatusAndDeletedAtIsNull(userId, couponId, status);
    }
}
