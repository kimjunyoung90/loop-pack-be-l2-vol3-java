package com.loopers.interfaces.api.coupon.response;

import com.loopers.application.coupon.result.UserCouponResult;
import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record MyCouponGetResponse(
        Long id,
        Long couponId,
        String couponName,
        DiscountType discountType,
        int discountValue,
        Integer minOrderAmount,
        String status,
        LocalDate expiredAt,
        ZonedDateTime createdAt
) {
    public static MyCouponGetResponse from(UserCouponResult result) {
        return new MyCouponGetResponse(
                result.id(),
                result.couponId(),
                result.couponName(),
                result.discountType(),
                result.discountValue(),
                result.minOrderAmount(),
                result.status(),
                result.expiredAt(),
                result.createdAt()
        );
    }
}
