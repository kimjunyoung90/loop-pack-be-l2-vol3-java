package com.loopers.application.coupon.result;

import com.loopers.domain.coupon.CouponIssueRequest;
import com.loopers.domain.coupon.CouponRequestStatus;

import java.time.ZonedDateTime;

public record CouponIssueRequestResult(
        Long id,
        Long userId,
        Long couponId,
        CouponRequestStatus status,
        String failReason,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static CouponIssueRequestResult from(CouponIssueRequest request) {
        return new CouponIssueRequestResult(
                request.getId(),
                request.getUserId(),
                request.getCouponId(),
                request.getStatus(),
                request.getFailReason(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
