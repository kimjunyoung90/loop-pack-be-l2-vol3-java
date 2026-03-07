package com.loopers.interfaces.api.coupon.response;

import com.loopers.application.coupon.result.UserCouponResult;
import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record IssueCouponResponse(
        Long id,
        String couponName,
        DiscountType discountType,
        int discountValue,
        String status,
        LocalDate expiredAt,
        ZonedDateTime createdAt
) {
    public static IssueCouponResponse from(UserCouponResult result) {
        return new IssueCouponResponse(
                result.id(),
                result.couponName(),
                result.discountType(),
                result.discountValue(),
                result.status(),
                result.expiredAt(),
                result.createdAt()
        );
    }
}
