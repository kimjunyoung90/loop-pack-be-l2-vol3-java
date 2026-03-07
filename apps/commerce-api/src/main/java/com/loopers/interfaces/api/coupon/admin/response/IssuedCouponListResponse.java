package com.loopers.interfaces.api.coupon.admin.response;

import com.loopers.application.coupon.result.UserCouponResult;
import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record IssuedCouponListResponse(
        Long id,
        Long userId,
        Long couponId,
        String couponName,
        DiscountType discountType,
        int discountValue,
        Integer minOrderAmount,
        String status,
        LocalDate expiredAt,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static IssuedCouponListResponse from(UserCouponResult result) {
        return new IssuedCouponListResponse(
                result.id(),
                result.userId(),
                result.couponId(),
                result.couponName(),
                result.discountType(),
                result.discountValue(),
                result.minOrderAmount(),
                result.status(),
                result.expiredAt(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
