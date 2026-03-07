package com.loopers.interfaces.api.coupon.admin.response;

import com.loopers.application.coupon.result.UserCouponResult;
import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record IssuedCouponGetResponse(
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
    public static IssuedCouponGetResponse from(UserCouponResult result) {
        return new IssuedCouponGetResponse(
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
