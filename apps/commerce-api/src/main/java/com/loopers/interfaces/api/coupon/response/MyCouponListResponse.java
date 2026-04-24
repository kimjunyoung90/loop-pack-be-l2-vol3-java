package com.loopers.interfaces.api.coupon.response;

import com.loopers.application.coupon.result.UserCouponResult;
import com.loopers.domain.common.Money;
import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record MyCouponListResponse(
        Long id,
        Long couponId,
        String couponName,
        DiscountType discountType,
        int discountValue,
        Money minOrderAmount,
        String status,
        LocalDate expiredAt,
        ZonedDateTime createdAt
) {
    public static MyCouponListResponse from(UserCouponResult result) {
        return new MyCouponListResponse(
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
