package com.loopers.interfaces.api.coupon.response;

import com.loopers.application.coupon.result.CouponIssueRequestResult;
import com.loopers.domain.coupon.CouponRequestStatus;

import java.time.ZonedDateTime;

public record CouponIssueRequestResponse(
        Long id,
        Long couponId,
        CouponRequestStatus status,
        String failReason,
        ZonedDateTime createdAt
) {
    public static CouponIssueRequestResponse from(CouponIssueRequestResult result) {
        return new CouponIssueRequestResponse(
                result.id(),
                result.couponId(),
                result.status(),
                result.failReason(),
                result.createdAt()
        );
    }
}
